import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {FormBuilder} from '@angular/forms';

@Component({
  selector: 'app-simple-json-editor',
  templateUrl: './simple-json-editor.component.html',
  styleUrls: ['./simple-json-editor.component.css']
})
export class SimpleJsonEditorComponent implements OnInit {
  @Input()
  dataHolder: any;

  @ViewChild(`newKey`) newKeyRef: ElementRef;
  @ViewChild(`metadataScroll`) metaScrollRef: ElementRef;

  constructor(private formBuilder: FormBuilder) { }

  metadataForm = this.formBuilder.group({
    newKey: [],
    newValue: []
  });

  ngOnInit() {
  }

  addMetadata(key, value) {
    if (!key || !value) { return; }

    if (this.dataHolder.metadata === undefined) {
      this.dataHolder.metadata = {};
    }

    // attempt to extract a number; on failure, keep as string
    const attemptNum: number = Number(value);
    if (!isNaN(attemptNum)) { value = attemptNum; }

    this.dataHolder.metadata[key] = value;

    this.metadataForm.reset();
    this.newKeyRef.nativeElement.focus();

    // async'd to delay scrolling until after metadata element added to UI
    setTimeout(() => this.metaScrollRef.nativeElement.scrollIntoView(false));
  }

  removeDataAtKey(key) {
    delete this.dataHolder.metadata[key];
  }
}
