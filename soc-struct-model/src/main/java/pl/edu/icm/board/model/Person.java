package pl.edu.icm.board.model;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Objects;

@WithMapper
public class Person {
    public enum Sex { M, K }

    private Sex sex;
    private int age;

    public Sex getSex() {
        return sex;
    }

    public int getAge() {
        return age;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return age == person.age &&
                sex == person.sex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sex, age);
    }

    @Override
    public String toString() {
        return "Person{" +
                "sex=" + sex +
                ", age=" + age +
                '}';
    }
}
