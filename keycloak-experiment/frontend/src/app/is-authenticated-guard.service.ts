import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';

@Injectable({
  providedIn: 'root'
})
export class IsAuthenticatedGuard implements CanActivate {
  constructor(
      private readonly router: Router,
      private readonly oauthService: OAuthService) {
  }

  async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    console.log('is authenticated', this.oauthService.getIdToken());
    if (this.oauthService.getIdToken() !== null) {
      // TODO: check session?
      return true;
    }
    this.router.navigateByUrl('/sign-in');
    return false;
  }
}
