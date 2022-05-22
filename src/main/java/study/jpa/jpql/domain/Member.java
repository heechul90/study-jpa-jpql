package study.jpa.jpql.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(name = "member_name")
    private String name;

    private int age;

    @Enumerated(EnumType.STRING)
    private MemberType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    //=== 생성자 메서드 ===//
    public Member(String name) {
        this.name = name;
    }

    public Member(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Member(String name, int age, Team team) {
        this.name = name;
        this.age = age;
        this.team = team;
    }

    public Member(String name, int age, MemberType type, Team team) {
        this.name = name;
        this.age = age;
        this.type = type;
        this.team = team;
    }

    //=== 연관관계 편의 메서드===//
    public void addTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

    //=== 변경 메서드===//
    public void changeName(String memberName) {
        this.name = memberName;
    }
}
