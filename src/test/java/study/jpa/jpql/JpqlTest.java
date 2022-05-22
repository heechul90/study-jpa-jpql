package study.jpa.jpql;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.jpa.jpql.domain.Address;
import study.jpa.jpql.domain.Member;
import study.jpa.jpql.domain.Team;
import study.jpa.jpql.dto.MemberDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class JpqlTest {

    @PersistenceContext
    EntityManager em;

    @Test
    public void jpqlTest() throws Exception{
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member member1 = new Member("spring1");
        Member member2 = new Member("spring2");
        em.persist(member1);
        em.persist(member2);
        em.flush();
        em.clear();

        //when
        //결과가 여러개, 없으면 빈 리스트 반환
        List<Member> resultList = em.createQuery("select m from Member m", Member.class).getResultList();
        assertThat(resultList.size()).isEqualTo(2);

        //결과가 무조건 하나, 없으면 NoResultException, 두개 이상이면 NonUniqueResultException 예외 발생
        //애매하다. spring data jpa는 optional로 반환한다.
        Team findTeam = em.createQuery("select t from Team t", Team.class).getSingleResult();
        assertThat(findTeam.getName()).isEqualTo("teamA");

        //파라미터 바인딩
        Member findMember = em.createQuery("select m from Member m where m.name = :memberName", Member.class)
                .setParameter("memberName", "spring1")
                .getSingleResult();
        assertThat(findMember.getName()).isEqualTo("spring1");
    }

    @Test
    @Rollback(value = false)
    public void projectionTest() throws Exception{
        //given
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member member1 = new Member("spring1", 33, teamA);
        Member member2 = new Member("spring2", 23, teamA);
        em.persist(member1);
        em.persist(member2);
        em.flush();
        em.clear();

        //when
        //엔티티 프로젝션
        List<Member> resultList1 = em.createQuery("select m from Member m", Member.class)
                .getResultList();
        Member findMember = resultList1.get(0);
        findMember.changeName("jpa1");

        //엔티티 프로젝션(조인)
        List<Team> resultList2 = em.createQuery("select t from Member m join m.team t", Team.class)
                .getResultList();

        //임베디드 타입 프로젝션
        List<Address> resultList3 = em.createQuery("select o.address from Order o", Address.class)
                .getResultList();

        //스칼라 타입 프로젝션
        List resultList4 = em.createQuery("select m.name, m.age from Member m")
                .getResultList();
        Object object = resultList4.get(0);
        Object[] result1 = (Object[]) object;
        System.out.println("result1[0] = " + result1[0]);
        System.out.println("result1[1] = " + result1[1]);

        List<Object[]> resultList5 = em.createQuery("select m.name, m.age from Member m")
                .getResultList();
        Object[] result2 = resultList5.get(0);
        System.out.println("result2[0] = " + result2[0]);
        System.out.println("result2[1] = " + result2[1]);

        List<MemberDto> resultList6 = em.createQuery("select new study.jpa.jpql.dto.MemberDto(m.name, m.age) from Member m", MemberDto.class)
                .getResultList();
    }

    @Test
    @Rollback(value = false)
    public void pagingTest() throws Exception{
        //given
        for (int i = 0; i < 100; i++) {
            String memberName = "member" + i;
            em.persist(new Member(memberName, i));
        }
        em.flush();
        em.clear();

        //when
        List<Member> resultList = em.createQuery("select m from Member m order by m.age desc", Member.class)
                .setFirstResult(0)
                .setMaxResults(10)
                .getResultList();
        for (Member member : resultList) {
            System.out.println("member.getName() = " + member.getName());
            System.out.println("member.getAge() = " + member.getAge());
        }
        assertThat(resultList.size()).isEqualTo(10);

        //then
    }
}
