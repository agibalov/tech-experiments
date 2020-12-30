import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { OAuthService } from 'angular-oauth2-oidc';

@Injectable({
  providedIn: 'root'
})
export class IsNotAuthenticatedGuard implements CanActivate {
  constructor(
      private readonly router: Router,
      private readonly oauthService: OAuthService) {
  }

  async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
    console.log('is not authenticated', this.oauthService.getIdToken());
    if (!this.oauthService.hasValidIdToken()) {
      return true;
    }
    this.router.navigateByUrl('/home');
    return false;
  }
}
