import {Component, Input, OnInit} from '@angular/core';
import {Mission} from '../../api/models/mission';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MissionStep} from '../../api/models/mission-step';
import {MissionStepViewModel} from '../../models/mission-step-view-model';

@Component({
  selector: 'app-mission-steps-card',
  templateUrl: './mission-steps-card.component.html',
  styleUrls: ['./mission-steps-card.component.css']
})
export class MissionStepsCardComponent implements OnInit {
  @Input() mission: Mission;
  // private stepForm: FormGroup;
  private newStep = new MissionStepViewModel();

  constructor(private formBuilder: FormBuilder) { }

  public stepForm = this.formBuilder.group({
    newDisplayName: [],
    newCount: [],
    newDescription: [],
    newStepNewRewardItem: [],
    newStepNewRewardCt: []
  });

  drop(event: CdkDragDrop<string[]>) {
    console.log(this.mission.steps);
    moveItemInArray(this.mission.steps, event.previousIndex, event.currentIndex);
  }

  ngOnInit() {
    for (let i = 0; i < this.mission.steps.length; i++) {
      this.stepForm.addControl('displayName' + i, new FormControl('', Validators.required));
      this.stepForm.addControl('description' + i, new FormControl('', Validators.required));
      this.stepForm.addControl('count' + i, new FormControl('', Validators.required/*, Validators.pattern('^[0-9]*$')*/));
      this.stepForm.addControl('step' + i + 'NewRewardItem', new FormControl('', Validators.pattern('^[a-zA-Z0-9]*$')));
      this.stepForm.addControl('step' + i + 'NewRewardCt', new FormControl('', Validators.pattern('^[0-9]*$')));
    }
  }
}
