import { Resolver, Query, Args, Mutation } from '@nestjs/graphql';
import { Todo } from './graphql';

@Resolver('Todos')
export class TodoResolver {
    private todos: Todo[] = [
        { id: '1', text: 'Get some coffee' },
        { id: '2', text: 'Get some milk '}
    ];

    @Query('todos')
    async getAllTodos(): Promise<Todo[]> {
        return this.todos;
    }

    @Query('todo')
    async getBook(
        @Args('id') id: string): Promise<Todo> {

        const todo = this.todos.find(t => t.id === id);
        if (todo === undefined) {
            return null;
        }
        return todo;
    }

    @Mutation('putTodo')
    async putTodo(
        @Args('id') id: string,
        @Args('text') text: string): Promise<Todo> {

        let todo = this.todos.find(t => t.id === id);
        if (todo === undefined) {
            todo = { id, text };
            this.todos.push(todo);
        } else {
            todo.text = text;
        }
        return todo;
    }

    @Mutation('deleteTodo')
    async deleteTodo(
        @Args('id') id: string): Promise<Todo> {

        this.todos = this.todos.filter(t => t.id !== id);
        return null;
    }
}
