import { Component, OnInit } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.scss']
})
export class HomePageComponent {
  result: any;

  constructor(
    private readonly oauthService: OAuthService,
    private readonly httpClient: HttpClient) {
  }

  get claims(): object {
    return this.oauthService.getIdentityClaims();
  }

  signOut() {
    this.oauthService.logOut();
  }

  async refresh() {
    await this.oauthService.silentRefresh();
  }

  async test() {
    this.result = await this.httpClient.get(
      '/api/hello',
      {
        headers: {
          Authorization: `Bearer ${this.oauthService.getIdToken()}`
        }
      }).toPromise();
  }
}
