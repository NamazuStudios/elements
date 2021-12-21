import {Component, OnInit} from '@angular/core';
import {ConfigService} from './config.service';
import {ApiConfiguration} from './api/api-configuration';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'ELEMENTS';

  constructor(private configService: ConfigService, private apiConfiguration: ApiConfiguration) { }

  ngOnInit() {
    this.configService.load().then(r => {
        this.apiConfiguration.rootUrl = this.configService.get().api.url;
      }
    );
  }
}
