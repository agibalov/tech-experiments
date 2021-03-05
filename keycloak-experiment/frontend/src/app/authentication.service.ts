import { Injectable } from '@angular/core';
import { OAuthService } from 'angular-oauth2-oidc';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private initializingPromise: Promise<void> = null;

  constructor(
    private readonly oauthService: OAuthService,
    private readonly router: Router) {

    this.oauthService.configure({
      issuer: 'http://localhost:8081/auth/realms/dummy2',
      redirectUri: window.location.origin + '/sign-in',
      clientId: 'app',
      responseType: 'code',
      scope: 'openid profile email',
      showDebugInformation: true,
      useSilentRefresh: true,
      silentRefreshRedirectUri: window.location.origin + '/silent-refresh.html',
      //timeoutFactor: 0.0083,
      timeoutFactor: 0.1,
      silentRefreshTimeout: 5000,
      sessionChecksEnabled: true
    });

    this.oauthService.events.subscribe(e => {
      console.log(new Date().toISOString(), e);

      if (e.type === 'logout') {
        this.router.navigate(['/sign-in']);
      }
    });
  }

  private async initialize() {
    console.log('Initializing');
    await this.oauthService.loadDiscoveryDocumentAndTryLogin();
    this.oauthService.setupAutomaticSilentRefresh();
  }

  private async ensureInitialized() {
    if (this.initializingPromise === null) {
      this.initializingPromise = this.initialize();
    }
    await this.initializingPromise;
  }

  public async isAuthenticated() {
    await this.ensureInitialized();
    return this.oauthService.hasValidIdToken();
  }
}
