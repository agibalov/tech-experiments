import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SignInPageComponent } from './sign-in-page/sign-in-page.component';
import { HomePageComponent } from './home-page/home-page.component';
import { IsAuthenticatedGuard } from './is-authenticated-guard.service';
import { IsNotAuthenticatedGuard } from './is-not-authenticated-guard.service';


const routes: Routes = [
  {
    path: '',
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'home'
      },
      {
        path: 'sign-in',
        canActivate: [ IsNotAuthenticatedGuard ],
        component: SignInPageComponent
      },
      {
        path: 'home',
        canActivate: [ IsAuthenticatedGuard ],
        component: HomePageComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
