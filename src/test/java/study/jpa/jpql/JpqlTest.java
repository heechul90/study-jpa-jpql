package study.jpa.jpql;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.jpa.jpql.domain.Member;
import study.jpa.jpql.domain.Team;

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
}
