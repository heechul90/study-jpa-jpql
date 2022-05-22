package study.jpa.jpql.domain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Test
    @Rollback(value = false)
    public void createMemberTest() throws Exception{
        //given
        Member member = new Member("spring");

        //when
        em.persist(member);
        em.flush();
        em.clear();

        //then
        Member findMember = em.find(Member.class, member.getId());
        assertThat(findMember.getName()).isEqualTo("spring");
    }

    @Test
    public void createMemberWithJpqlTest() throws Exception{
        //given

        //when

        //then
    }

}