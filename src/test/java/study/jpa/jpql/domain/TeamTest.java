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
class TeamTest {

    @PersistenceContext
    EntityManager em;

    @Test
    @Rollback(value = false)
    public void createTeamTest() throws Exception{
        //given
        Team team = new Team("teamA");

        //when
        em.persist(team);
        em.flush();
        em.clear();

        //then
        Team findTeam = em.find(Team.class, team.getId());
        assertThat(findTeam.getName()).isEqualTo("teamA");
    }

}