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
  @ViewChildren('.existing-step-reward-editor') rewardEditors: QueryList<MissionRewardsEditorComponent>;

  public stepForm = this.formBuilder.group({});

  constructor(private formBuilder: FormBuilder) { }

  // TODO depends on validity of existingstepform, finalstepform, finalsteprewards, existingsteprewards

  public stepsValid() {
    // invalid if neither steps nor final step exist
    if (this.mission.steps.length == 0 && !this.mission.finalRepeatStep) {
      //console.log("No steps or final step");
      return false;
    }

    // invalid if final step or prelim step doesn't have rewards
    for(let i = 0; i < this.mission.steps.length; i++) {
      if (this.mission.steps[i].rewards.length == 0) return false;
    }
    if(this.mission.finalRepeatStep && this.mission.finalRepeatStep.rewards.length == 0) return false;

    // all existing steps must be valid
    if (!this.stepForm.valid) {
      return false;
    }

    // all existing rewards must be valid
    if (!this.rewardEditors) {
      //console.log("No reward editors");
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

  addStep() {
    const newStep = new MissionStepViewModel();
    newStep.isNew = true;
    this.addStepControl(this.mission.steps.length, newStep);
    this.mission.steps.push(newStep);
  }

  deleteStep(index: number) {
    this.mission.steps.splice(index, 1);
    this.removeStepControl(index);
  }

  deleteFinalStep() {
    delete this.mission.finalRepeatStep;
    this.removeFinalStepControl();
  }

  addStepControl(index: number, step: MissionStep) {
    this.stepForm.addControl('displayName' + index, new FormControl(step.displayName, Validators.required));
    this.stepForm.addControl('description' + index, new FormControl(step.description, Validators.required));
    this.stepForm.addControl('count' + index, new FormControl(step.count, [Validators.required, Validators.pattern('^[0-9]*$')]));
  }

  removeStepControl(i: number) {
    this.stepForm.removeControl('displayName' + i);
    this.stepForm.removeControl('description' + i);
    this.stepForm.removeControl('count' + i);
  }

  addFinalStepControl(finalStep: MissionStep) {
    this.stepForm.addControl('finalDisplayName', new FormControl(finalStep.displayName, Validators.required));
    this.stepForm.addControl('finalDescription', new FormControl(finalStep.description, Validators.required));
    this.stepForm.addControl('finalCount', new FormControl(finalStep.count, [Validators.required, Validators.pattern('^[0-9]*$')]));
  }

  removeFinalStepControl() {
    this.stepForm.removeControl('finalDisplayName');
    this.stepForm.removeControl('finalDescription');
    this.stepForm.removeControl('finalCount');
  }

  drop(event: CdkDragDrop<string[]>) {
    moveItemInArray(this.mission.steps, event.previousIndex, event.currentIndex);
  }

  toggleFinalStep(event: any, stepIndex: number) {
    if (event.checked) {
      // remove step from steps array and set as final step
      const finalStep = this.mission.steps.splice(stepIndex, 1)[0];
      // remove step from stepform to preserve form validity
      this.removeStepControl(this.mission.steps.length);

      // add final step controls
      this.addFinalStepControl(finalStep);
      this.mission.finalRepeatStep = finalStep;
    } else {
      // move final step to steps array; unset final step
      const finalStep = this.mission.finalRepeatStep;
      delete this.mission.finalRepeatStep;
      // remove final step controls to preserve form validity
      this.removeFinalStepControl();

      // add control to stepform to accommodate new step in array
      this.addStepControl(this.mission.steps.length, finalStep);
      this.mission.steps.push(finalStep);
    }
  }

  ngOnInit() {
    if (!this.mission.steps) { this.mission.steps = []; }

    if (this.mission.steps) {
      for (let i = 0; i < this.mission.steps.length; i++) {
        this.addStepControl(i, this.mission.steps[i]);
      }
    }

    if (this.mission.finalRepeatStep) {
      this.addFinalStepControl(this.mission.finalRepeatStep);
    }
  }
}
