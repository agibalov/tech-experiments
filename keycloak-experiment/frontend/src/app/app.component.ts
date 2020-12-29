import { Component } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor(
    private readonly oauthService: OAuthService,
    private readonly router: Router) {

    this.oauthService.events.subscribe(e => {
      console.log(new Date().toISOString(), e);

      if (e.type === 'logout') {
        this.router.navigate(['/sign-in']);
      }
    });
  }
}
