import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { DataService } from './data.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    {
      provide: DataService,
      useValue: new DataService('admin', 'qwerty')
    },
    {
      provide: APP_INITIALIZER,
      useFactory: (dataService: DataService) => {
        return async () => await dataService.initialize();
      },
      multi: true,
      deps: [DataService]
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
