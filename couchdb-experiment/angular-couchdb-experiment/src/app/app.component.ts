import { Component, NgZone, OnDestroy, OnInit } from '@angular/core';
import { DataService } from './data.service';
import { of, Subscription } from 'rxjs';
import { TaskDocument } from './task-document';
import { v4 as uuid } from 'uuid';
import { concatMap, tap } from 'rxjs/operators';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {
  private tasksSubscription: Subscription;
  private changesSubscription: Subscription;
  tasks: TaskDocument[] = [];
  isOnline = false;
  formGroup: FormGroup;

  constructor(
    private readonly dataService: DataService,
    private readonly zone: NgZone,
    formBuilder: FormBuilder) {

    this.formGroup = formBuilder.group({
      text: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.tasksSubscription = this.dataService.tasks.find().$.subscribe(tasks => {
      this.zone.run(() => {
        this.tasks = tasks;
      });
    });

    this.changesSubscription = this.dataService.replicationState$
      .pipe(
        tap(replicationState => console.log('rs', replicationState)),
        tap(replicationState => this.isOnline = replicationState != null),
        concatMap(replicationState => replicationState != null ? replicationState.change$ : of(null))
      )
      .subscribe(change => console.log('change', change));
  }

  ngOnDestroy(): void {
    this.tasksSubscription.unsubscribe();
    this.changesSubscription.unsubscribe();
  }

  async handleCreateTask() {
    const value: { text: string } = this.formGroup.getRawValue();
    this.formGroup.reset();

    await this.dataService.tasks.insert({
      id: uuid(),
      text: value.text,
      status: 'Todo'
    });
  }

  async handleDeleteTask(taskId: string) {
    await this.dataService.tasks.findOne(taskId).remove();
  }

  goOnline() {
    this.dataService.goOnline();
  }

  goOffline() {
    this.dataService.goOffline();
  }
}
