import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable()
export class ConfigService {
  constructor(private http: HttpClient) { }

  private appConfig;

  async load(): Promise<void> {
    const storedAppConfig = JSON.parse(localStorage.getItem('appConfig'));

    if(!storedAppConfig){
      return this.http.get('./config.json')
      .toPromise()
      .then(data => {
        this.appConfig = data;
        localStorage.setItem('appConfig', JSON.stringify(this.appConfig));
      })
      .catch(reason => {
        console.log(reason);
      });
    }
    else {
      this.appConfig = storedAppConfig;
      return Promise.resolve(storedAppConfig) ;
    }
  }


  get() {
    return this.appConfig;
  }
}
