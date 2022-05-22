package study.jpa.jpql.dto;

public class MemberDto {

    private String memberName;
    private int age;

    public MemberDto(String memberName, int age) {
        this.memberName = memberName;
        this.age = age;
    }
}
