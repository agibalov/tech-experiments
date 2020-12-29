import { Component } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';

@Component({
  selector: 'app-sign-in-page',
  templateUrl: './sign-in-page.component.html',
  styleUrls: ['./sign-in-page.component.scss']
})
export class SignInPageComponent {
  constructor(private readonly oauthService: OAuthService) {
  }

  signIn() {
    this.oauthService.initCodeFlow();
  }
}
