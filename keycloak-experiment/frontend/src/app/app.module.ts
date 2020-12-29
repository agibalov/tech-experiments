import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { OAuthModule } from 'angular-oauth2-oidc';
import { HttpClientModule } from '@angular/common/http';
import { HomePageComponent } from './home-page/home-page.component';
import { SignInPageComponent } from './sign-in-page/sign-in-page.component';
import { IsAuthenticatedGuard } from './is-authenticated-guard.service';
import { IsNotAuthenticatedGuard } from './is-not-authenticated-guard.service';
import { AuthenticationInitializerGuard } from './authentication-initializer-guard.service';

@NgModule({
  declarations: [
    AppComponent,
    HomePageComponent,
    SignInPageComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    OAuthModule.forRoot()
  ],
  providers: [
    AuthenticationInitializerGuard,
    IsAuthenticatedGuard,
    IsNotAuthenticatedGuard
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
