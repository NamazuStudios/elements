import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {Mission} from '../../api/models/mission';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';
import {AbstractControl, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MissionStepViewModel} from '../../models/mission-step-view-model';
import {MissionRewardsEditorComponent} from '../mission-rewards-editor/mission-rewards-editor.component';
import {MissionStep} from '../../api/models/mission-step';

@Component({
  selector: 'app-mission-steps-card',
  templateUrl: './mission-steps-card.component.html',
  styleUrls: ['./mission-steps-card.component.css']
})
export class MissionStepsCardComponent implements OnInit {
  @Input() mission: Mission;
  @ViewChild('newStepRewards') newStepRewards: MissionRewardsEditorComponent;
  @ViewChild('finalStepRewards') finalStepRewards: MissionRewardsEditorComponent;

  // private stepForm: FormGroup;
  public newStep = new MissionStepViewModel();
  public finalStep = new MissionStepViewModel();

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

  static triggerValidation(controlArray: {[key: string]: AbstractControl}) {
    // touch each control to trigger validation
    for (const i in controlArray) {
      if (controlArray.hasOwnProperty(i)) {
        controlArray[i].markAsTouched();
      }
    }
  }

  constructor(private formBuilder: FormBuilder) { }

  drop(event: CdkDragDrop<string[]>) {
    console.log(this.mission.steps);
    moveItemInArray(this.mission.steps, event.previousIndex, event.currentIndex);
  }

  // TODO attach newStep to mission, create new MissionStep instance for newStep
  addStepToMission() {
    const formData = this.newStepForm.value;
    this.newStep.description = formData.newDescription;
    this.newStep.count = formData.newCount;
    this.newStep.displayName = formData.newDisplayName;

    this.clearNewStepForm();

    this.mission.steps.push(this.newStep);

    // add form controls
    this.addExistingStepControl(this.mission.steps.length - 1, this.mission.steps[this.mission.steps.length - 1]);

    this.newStep = new MissionStepViewModel();
    this.newStepRewards.rewards = this.newStep.rewards;
  }

  // TODO clear step form, reward form, metadata
  clearNewStepForm() {
    this.newStepForm.reset();
    this.newStepRewards.newRewardForm.reset();
    this.newStepRewards.existingRewardForm.reset();
  }

  addExistingStepControl(index: number, step: MissionStep) {
    this.existingStepForm.addControl('displayName' + index, new FormControl(step.displayName, Validators.required));
    this.existingStepForm.addControl('description' + index, new FormControl(step.description, Validators.required));
    this.existingStepForm.addControl('count' + index, new FormControl(step.count, [Validators.required, Validators.pattern('^[0-9]*$')]));
  }

  clearFinalStepForm() {
    this.finalStepForm.reset();
    this.finalStepRewards.newRewardForm.reset();
    this.finalStepRewards.existingRewardForm.reset();

    this.mission.finalRepeatStep = null;
  }

  addFinalStepToMission() {
    const formData = this.finalStepForm.value;
    this.finalStep.description = formData.finalDescription;
    this.finalStep.displayName = formData.finalDisplayName;
    this.finalStep.count = formData.finalCount;

    this.mission.finalRepeatStep = this.finalStep;
  }

  ngOnInit() {
    if (this.mission.finalRepeatStep) { this.finalStep = this.mission.finalRepeatStep; }

    if (this.mission.steps) {
      for (let i = 0; i < this.mission.steps.length; i++) {
        this.addExistingStepControl(i, this.mission.steps[i]);
      }
    }
  }
}
