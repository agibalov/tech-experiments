import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationInitializerGuard implements CanActivate {
    constructor(private readonly oauthService: OAuthService) {
    }

    async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
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

      await this.oauthService.loadDiscoveryDocumentAndTryLogin();

      this.oauthService.setupAutomaticSilentRefresh();

      return true;
    }
}
