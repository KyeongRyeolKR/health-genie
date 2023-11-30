package com.example.healthgenie.boundedContext.todo.controller;

import com.example.healthgenie.boundedContext.todo.dto.TodoRequestDto;
import com.example.healthgenie.boundedContext.todo.dto.TodoResponseDto;
import com.example.healthgenie.boundedContext.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calender/todo")
@Slf4j
public class TodoController {

    private final TodoService todoService;

    @PostMapping("/write") // http://localhost:1234/calender/todo/write
    public ResponseEntity addTodo(@RequestBody TodoRequestDto dto){

        TodoResponseDto result = todoService.addTodoList(dto);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @GetMapping("/{date}/{userId}") // http://localhost:1234/calender/todo/{date}/{userId}
    public List<TodoResponseDto> getTodos(@PathVariable LocalDate date , @PathVariable Long userId) {

        return todoService.getAllMyTodo(date,userId);
    }

    // 수정
    @PatchMapping("/update/{todoId}") // http://localhost:1234/calender/todo/update/{todoId}
    public ResponseEntity updateTodo(@RequestBody TodoRequestDto dto, @PathVariable Long todoId){

        TodoResponseDto response = todoService.update(dto,todoId);
        return new ResponseEntity(response,HttpStatus.OK);
    }

    // 본인만 삭제 가능하게 하기 -> 프론트에서 기능을 숨기면 되어서 구별 로직뺌
    @DeleteMapping("/delete/{todoId}") // http://localhost:1234/calender/todo/delete/{todoId}
    public ResponseEntity deleteTodo(@PathVariable Long todoId) {

        todoService.deletePtReview(todoId);

        return new ResponseEntity("todo가 삭제가 성공했습니다",HttpStatus.OK);
    }
}
