import { enableProdMode } from '@angular/core';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import { IptpiDemoAppModule, environment } from './app/';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(IptpiDemoAppModule);
