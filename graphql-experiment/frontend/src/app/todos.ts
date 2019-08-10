import { Mutation, Query } from 'apollo-angular';
import gql from 'graphql-tag';
import { Injectable } from '@angular/core';

export interface Todo {
  id: string;
  text: string;
}

@Injectable({
  providedIn: 'root',
})
export class AllTodosQuery extends Query<{ todos: Todo[] }> {
  document = gql`
    {
      todos { id text }
    }
  `;
}

export interface PutTodoVariables {
  id: string;
  text: string;
}

@Injectable({
  providedIn: 'root',
})
export class PutTodoMutation extends Mutation<Todo, PutTodoVariables> {
  document = gql`
    mutation createTodo($id: String!, $text: String!) {
      putTodo(id: $id, text: $text) { id text }
    }
  `;
}

export interface DeleteTodoVariables {
  id: string;
}

@Injectable({
  providedIn: 'root',
})
export class DeleteTodoMutation extends Mutation<Todo, DeleteTodoVariables> {
  document = gql`
    mutation deleteTodo($id: String!) {
      deleteTodo(id: $id) { id text }
    }
  `;
}
