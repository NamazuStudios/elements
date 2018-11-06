import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {BehaviorSubject} from "rxjs";

@Injectable()
export class ConfigService {
  constructor(private http: HttpClient) { }

  private appConfig;

  load() {

    return this.http.get('./assets/config.json')
      .toPromise()
      .then(data => {
        this.appConfig = data;
      })
      .catch(reason => {
        console.log(reason);
    });
  }


  get() {
    return this.appConfig;
  }
}
