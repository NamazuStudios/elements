import {Component, Input, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
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
  @ViewChildren('.existing-step-reward-editor') rewardEditors: QueryList<MissionRewardsEditorComponent>;

  // private stepForm: FormGroup;
  public newStep = new MissionStepViewModel();
  public finalStep = new MissionStepViewModel();
  public isFinalStepChanged = false;

  public newStepForm = this.formBuilder.group({
    newDisplayName: ['', [Validators.required]],
    newCount: ['', [Validators.required, Validators.pattern('^[0-9]*$')]],
    newDescription: ['', [Validators.required]],
  });

  public existingStepForm = this.formBuilder.group({});

  public finalStepForm = this.formBuilder.group({
    finalDisplayName: [ this.finalStep.displayName, [Validators.required]],
    finalCount: [this.finalStep.count, [Validators.required, Validators.pattern('^[0-9]*$')]],
    finalDescription: [this.finalStep.description, [Validators.required]]
  });

  constructor(private formBuilder: FormBuilder) { }

  // TODO depends on validity of existingstepform, finalstepform, finalsteprewards, existingsteprewards

  public stepsValid() {
    // invalid if neither steps nor final step exist
    if (this.mission.steps.length == 0 && !(this.mission.finalRepeatStep.rewards.length > 0
        && this.mission.finalRepeatStep.count && this.mission.finalRepeatStep.description && this.mission.finalRepeatStep.displayName)) {
      //console.log("No steps or final step");
      return false;
    }

    // all existing steps must be valid
    if (!this.existingStepForm.valid) {
      return false;
    }

    // all existing rewards must be valid
    if (!this.rewardEditors) {
      console.log("No reward editors");
      return true;
    }
    const rewardEditors = this.rewardEditors.toArray();
    for (let i = 0; i < rewardEditors.length; i++) {
      const rewardEditor = rewardEditors[i];

      if (!rewardEditor.existingRewardForm.valid) {
        //console.log("Invalid existing reward form");
        //console.log(rewardEditor);
        return false;
      }
    }

    //console.log("All steps valid");
    // all validity tests passed
    return true;
  }

  updateStep(step: MissionStep, param: string, event: any) {
    step[param] = event.target.value;
  }

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

  ngOnInit() {
    if (this.mission.finalRepeatStep) { this.finalStep = this.mission.finalRepeatStep; }
    if (!this.mission.steps) { this.mission.steps = []; }

    if (this.mission.steps) {
      for (let i = 0; i < this.mission.steps.length; i++) {
        this.addExistingStepControl(i, this.mission.steps[i]);
      }
    }
  }
}
