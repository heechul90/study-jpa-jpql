package study.jpa.jpql;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.jpa.jpql.domain.Address;
import study.jpa.jpql.domain.Member;
import study.jpa.jpql.domain.MemberType;
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

    /**
     * 기본 문법과 쿼리 API
     */
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

    /**
     * 프로젝션(select)
     */
    @Test
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

    /**
     * 페이징
     */
    @Test
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

    /**
     * 조인
     */
    @Test
    public void joinTest() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 10; i++) {
            String memberName = "member" + i;
            em.persist(new Member(memberName, i, (i % 2 == 0 ? teamA : teamB)));
        }
        em.flush();
        em.clear();

        //when
        //(inner) join
        List<Member> resultList1 = em.createQuery("select m from Member m inner join m.team", Member.class)
                .getResultList();

        //left (outer) join
        List<Member> resultList2 = em.createQuery("select m from Member m left outer join m.team", Member.class)
                .getResultList();

        //setter join
        List<Member> resultList3 = em.createQuery("select m from Member m, Team t where m.name = t.name", Member.class)
                .getResultList();

        //연관관계 on 절
        List<Member> resultList4 = em.createQuery("select m from Member m left join m.team t on t.name = :teamName", Member.class)
                .setParameter("teamName", "teamA")
                .getResultList();

        //연관관계가 없는 on 절
        List<Member> resultList5 = em.createQuery("select m from Member m left join Team t on t.id = m.team.id", Member.class)
                .getResultList();
    }

    /**
     * 서브쿼리
     */
    @Test
    public void subQueryTest() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 10; i++) {
            String memberName = "member" + i;
            em.persist(new Member(memberName, i, (i % 2 == 0 ? teamA : teamB)));
        }
        em.flush();
        em.clear();

        //when
        List<Object[]> resultList1 = em.createQuery("select (select avg(subm.age) from Member subm) from Member m")
                .getResultList();

        List<Member> resultList2 = em.createQuery("select m from Member m where m.age > (select avg(m2.age) from Member m2)", Member.class)
                .getResultList();

        List<Member> resultList3 = em.createQuery("select m from Member m where (select count(o) from Order o where m = o.member) > 0", Member.class)
                .getResultList();
    }

    /**
     * JPQL 타입 표현과 기타식
     * @throws Exception
     */
    @Test
    public void jpqlTypeTest() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 10; i++) {
            String memberName = "member" + i;
            em.persist(new Member(memberName, i, (i % 2 == 0 ? MemberType.USER: MemberType.ADMIN), (i % 2 == 0 ? teamA : teamB)));
        }
        em.flush();
        em.clear();
        
        //when
        List<Object[]> resultList1 = em.createQuery("select m.name, 'hello', true, m.type from Member m" +
                        " where m.type = study.jpa.jpql.domain.MemberType.ADMIN")
                .getResultList();
        Object[] result = resultList1.get(0);
        System.out.println("result[0] = " + result[0]);
        System.out.println("result[1] = " + result[1]);
        System.out.println("result[2] = " + result[2]);
        System.out.println("result[3] = " + result[3]);
    }

    /**
     * 조건식(case 등등)
     */
    @Test
    public void ConditionTest() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 100; i++) {
            String memberName = "member" + i;
            em.persist(new Member(memberName, i, (i % 2 == 0 ? MemberType.USER: MemberType.ADMIN), (i % 2 == 0 ? teamA : teamB)));
        }
        em.flush();
        em.clear();
        
        //when
        //case
        List<String> resultList1 = em.createQuery("select" +
                        " case" +
                        "    when m.age <= 20 then '학생'" +
                        "    when m.age <= 60 then '노동자'" +
                        "    else '노인'" +
                        " end" +
                        " from Member m", String.class)
                .getResultList();
        for (String s : resultList1) {
            System.out.println("s = " + s);
        }

        Member findMember = em.find(Member.class, 3L);
        findMember.changeName(null);

        //coalesce
        List<String> resultList2 = em.createQuery("select coalesce(m.name, '이름 없는 회원') from Member m", String.class)
                .setFirstResult(0)
                .setMaxResults(10)
                .getResultList();
        for (String s : resultList2) {
            System.out.println("s = " + s);
        }

        //nullif
        List<String> resultList3 = em.createQuery("select nullif(m.name, 'member6') from Member m", String.class)
                .setFirstResult(0)
                .setMaxResults(10)
                .getResultList();
        for (String s : resultList3) {
            System.out.println("s = " + s);
        }
    }

    /**
     * jpql 함수
     */
    @Test
    public void jqplFunctionTest() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 10; i++) {
            String memberName = "  member" + i;
            em.persist(new Member(memberName, i, (i % 2 == 0 ? MemberType.USER: MemberType.ADMIN), (i % 2 == 0 ? teamA : teamB)));
        }
        em.flush();
        em.clear();

        //when
        //concat
        List<String> resultList1 = em.createQuery("select concat('a', 'b') from Member m", String.class)
                .getResultList();
        for (String s : resultList1) {
            System.out.println("s = " + s);
        }

        //substring
        List<String> resultList2 = em.createQuery("select substring(m.name, 2, 5) from Member m", String.class)
                .getResultList();
        for (String s : resultList2) {
            System.out.println("s = " + s);
        }

        //trim
        List<String> resultList3 = em.createQuery("select trim(m.name) from Member m", String.class)
                .getResultList();
        for (String s : resultList3) {
            System.out.println("s = " + s);
        }

        //lower, upper
        List<String> resultList4 = em.createQuery("select upper(m.name) from Member m", String.class)
                .getResultList();
        for (String s : resultList4) {
            System.out.println("s = " + s);
        }

        //lower, upper
        List<Integer> resultList5 = em.createQuery("select length(m.name) from Member m", Integer.class)
                .getResultList();
        for (Integer i : resultList5) {
            System.out.println("i = " + i);
        }

        //locate
        List<Integer> resultList6 = em.createQuery("select locate('de', 'abcdefg') from Member m", Integer.class)
                .getResultList();
        for (Integer i : resultList6) {
            System.out.println("i = " + i);
        }

        //locate
        List<Integer> resultList7 = em.createQuery("select size(t.members) from Team t", Integer.class)
                .getResultList();
        for (Integer i : resultList7) {
            System.out.println("i = " + i);
        }
    }

    /**
     * 경로 표현식
     */
    @Test
    public void 경로_표현식() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 10; i++) {
            String memberName = "  member" + i;
            em.persist(new Member(memberName, i, (i % 2 == 0 ? MemberType.USER: MemberType.ADMIN), (i % 2 == 0 ? teamA : teamB)));
        }
        em.flush();
        em.clear();

        //when
        em.createQuery("select o.member.team from Order o")
                .getResultList();

        em.createQuery("select t.members from Team t")
                .getResultList();

        em.createQuery("select m.name from Team t join t.members m")
                .getResultList();
    }

    @Test
    public void fetchJoin1Test() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 10; i++) {
            String memberName = "  member" + i;
            em.persist(new Member(memberName, i, (i % 2 == 0 ? MemberType.USER: MemberType.ADMIN), (i % 2 == 0 ? teamA : teamB)));
        }
        em.flush();
        em.clear();

        //when
        //n+1 문제 발생
        List<Member> resultList1 = em.createQuery("select m from Member m", Member.class)
                .getResultList();
        for (Member member : resultList1) {
            System.out.println("member.getName() = " + member.getName());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
        em.clear();

        //fetch join(지연로딩으로 해도 fetch를 우선순위로 한방쿼리로 다 가져온다.)
        List<Member> resultList2 = em.createQuery("select m from Member m join fetch m.team", Member.class)
                .getResultList();
        for (Member member : resultList2) {
            System.out.println("member.getName() = " + member.getName());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }
        em.clear();

        //일대다 fetch join(결과가 뻥튀기 된다.)
        List<Team> resultList3 = em.createQuery("select t from Team t join fetch t.members", Team.class)
                .getResultList();
        for (Team team : resultList3) {
            System.out.println("team.getName() = " + team.getName());
            for (Member member : team.getMembers()) {
                System.out.println("member = " + member.getName());
            }
        }
        em.clear();

        //일대단 fetch join distinct
        List<Team> resultList4 = em.createQuery("select distinct t from Team t join fetch t.members", Team.class)
                .getResultList();
        for (Team team : resultList4) {
            System.out.println("team.getName() = " + team.getName());
            for (Member member : team.getMembers()) {
                System.out.println("member = " + member.getName());
            }
        }
        em.clear();
    }

    /**
     * 엔티티 직접 사용
     */
    @Test
    public void 엔티티_직접_사용_Test() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 10; i++) {
            String memberName = "  member" + i;
            em.persist(new Member(memberName, i, (i % 2 == 0 ? MemberType.USER: MemberType.ADMIN), (i % 2 == 0 ? teamA : teamB)));
        }
        em.flush();
        em.clear();

        //when
        //resultList1 == resultList2 같은 쿼리가 나간다.
        List<Member> resultList1 = em.createQuery("select count(m) from Member m", Member.class)
                .getResultList();

        List<Member> resultList2 = em.createQuery("select count(m.id) from Member m", Member.class)
                .getResultList();

        //findMember1 == findMember2 같은 쿼리가 나간다.
        Member findMember1 = em.createQuery("select m from Member m where m.id = :memberId", Member.class)
                .setParameter("memberId", 1L)
                .getSingleResult();

        Member findMember2 = em.createQuery("select m from Member m where m.id = :member", Member.class)
                .setParameter("member", new Member())
                .getSingleResult();

        //resultList3 == resultList4 같은 쿼리가 나간다.
        List<Member> resultList3 = em.createQuery("select m from Member m where m.team = :team", Member.class)
                .setParameter("team", teamA)
                .getResultList();

        List<Member> resultList4 = em.createQuery("select m from Member m where m.team.id = :teamId", Member.class)
                .setParameter("teamId", 1L)
                .getResultList();
    }

    /**
     * 네임드 쿼리
     */
    @Test
    public void namedQueryTest() throws Exception{
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        for (int i = 0; i < 10; i++) {
            String memberName = "  member" + i;
            em.persist(new Member(memberName, i, (i % 2 == 0 ? MemberType.USER: MemberType.ADMIN), (i % 2 == 0 ? teamA : teamB)));
        }
        em.flush();
        em.clear();
        
        //when
        List<Member> resultList1 = em.createNamedQuery("Member.findByName", Member.class)
                .setParameter("memberName", "member3")
                .getResultList();

        //then
    }
}
