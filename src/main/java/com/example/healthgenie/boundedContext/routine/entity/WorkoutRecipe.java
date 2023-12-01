package com.example.healthgenie.boundedContext.routine.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.List;

@Embeddable // 내장 타입
@Getter
public class WorkoutRecipe {
    private String name;
    private int kg;
    private int sets;
    private int reps;

    protected WorkoutRecipe() {
        // 기본 생성자가 많이 사용되는 기술들이 많기에 만들어 주는 것이 좋다.
        // protected로 설정해야 안전하다
    }

    /*
        값 같은 경우는 변경 불가능하게 처음에 데이터를 주고, setter로 변경되지 않도록 한다.
     */
    public WorkoutRecipe(String name, int kg, int sets, int reps) {
        this.name = name;
        this.kg = kg;
        this.sets = sets;
        this.reps = reps;
    }
    public void updateName(String name) {
        this.name = name;
    }
    public void updateSets(int sets) {
        this.sets = sets;
    }
    public void updateKg(int kg) {
        this.kg = kg;
    }
    public void updateReps(int reps) {
        this.reps = reps;
    }
}
