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

  constructor(private formBuilder: FormBuilder) { }

  metadataForm = this.formBuilder.group({
    newKey: [],
    newValue: []
  });

  ngOnInit() {
  }

  addMetadata(key, value) {
    if (!key || !value) return;

    if (this.dataHolder.metadata === undefined) {
      this.dataHolder.metadata = {};
    }
    this.dataHolder.metadata[key] = value;

    this.metadataForm.reset();
    this.newKeyRef.nativeElement.focus();
  }

  removeDataAtKey(key) {
    delete this.dataHolder.metadata[key];
  }
}
