import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {Mission} from '../../api/models/mission';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MissionStepViewModel} from '../../models/mission-step-view-model';
import {MissionRewardsEditorComponent} from '../mission-rewards-editor/mission-rewards-editor.component';

@Component({
  selector: 'app-mission-steps-card',
  templateUrl: './mission-steps-card.component.html',
  styleUrls: ['./mission-steps-card.component.css']
})
export class MissionStepsCardComponent implements OnInit {
  @Input() mission: Mission;
  @ViewChild('newStepRewards') newStepRewards: MissionRewardsEditorComponent;

  // private stepForm: FormGroup;
  public newStep = new MissionStepViewModel();
  public finalStep = new MissionStepViewModel();
  public isNewStepValid = true;
  public isFinalStepValid;
  public isStepsValid;

  constructor(private formBuilder: FormBuilder) { }

  public newStepForm = this.formBuilder.group({
    newDisplayName: ['', [Validators.required]],
    newCount: ['', [Validators.required]],
    newDescription: ['', [Validators.required]],
  });

  public existingStepForm = this.formBuilder.group({});

  public finalStepForm = this.formBuilder.group({
    finalDisplayName: [ this.finalStep.displayName],
    finalCount: [this.finalStep.count, [Validators.required]],
    finalDescription: [this.finalStep.description]
  });

  drop(event: CdkDragDrop<string[]>) {
    console.log(this.mission.steps);
    moveItemInArray(this.mission.steps, event.previousIndex, event.currentIndex);
  }

  // TODO attach newStep to mission, create new MissionStep instance for newStep
  addStepToMission() {

    this.clearNewStepForm();
  }

  // TODO clear step form, reward form, metadata
  clearNewStepForm() {

  }

  ngOnInit() {
    if (this.mission.finalRepeatStep) { this.finalStep = this.mission.finalRepeatStep; }

    if (this.mission.steps) {
      for (let i = 0; i < this.mission.steps.length; i++) {
        this.stepForm.addControl('displayName' + i, new FormControl('', Validators.required));
        this.stepForm.addControl('description' + i, new FormControl('', Validators.required));
        this.stepForm.addControl('count' + i, new FormControl('', Validators.required/*, Validators.pattern('^[0-9]*$')*/));
        this.stepForm.addControl('step' + i + 'NewRewardItem', new FormControl('', Validators.pattern('^[a-zA-Z0-9]*$')));
        this.stepForm.addControl('step' + i + 'NewRewardCt', new FormControl('', Validators.pattern('^[0-9]*$')));
      }
    }
  }
}
