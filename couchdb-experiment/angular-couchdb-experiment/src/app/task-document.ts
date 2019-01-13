import { RxCollection, RxCollectionCreator, RxDocument } from 'rxdb';

export const TASK_SCHEMA: RxCollectionCreator = {
  name: 'tasks',
  schema: {
    version: 0,
    title: 'Task',
    type: 'object',
    properties: {
      id: {
        type: 'string',
        primary: true
      },
      text: {
        type: 'string'
      },
      status: {
        type: 'string'
      }
    },
    required: [ 'text', 'status' ]
  }
};

export type TaskStatus = 'Todo' | 'InProgress' | 'Done';

export const ALL_TASK_STATUSES: TaskStatus[] = [
  'Todo',
  'InProgress',
  'Done'
];

export interface TaskDocumentType {
  id: string;
  text: string;
  status: TaskStatus;
}

export type TaskDocument = RxDocument<TaskDocumentType>;

export interface TaskCollection extends RxCollection<TaskDocumentType> {
}
