import { Component, OnInit } from '@angular/core';
import {SeverVersionMetadataService} from "../api/services/sever-version-metadata.service";
import {Version} from "../api/models/version";
import {BehaviorSubject, Observable} from "rxjs";
import { VERSION as gitVersion } from '../../environments/version';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  version = new BehaviorSubject<string>("...");
  revision = new BehaviorSubject<string>("...");
  timestamp = new BehaviorSubject<string>("...");

  currentGitHash = gitVersion.hash;

  constructor(private severVersionMetadataService: SeverVersionMetadataService) { }

  ngOnInit() {
    this.severVersionMetadataService
      .getVersion()
      .subscribe(result => {
        this.version.next(result.version);
        this.revision.next(result.revision);
        this.timestamp.next(result.timestamp);
      });
  }
}
