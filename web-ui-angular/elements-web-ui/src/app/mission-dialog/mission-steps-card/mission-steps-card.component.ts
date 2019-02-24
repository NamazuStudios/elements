import {AfterContentInit, Component, Input, OnInit} from '@angular/core';
import {Mission} from '../../api/models/mission';
import {MissionStepViewModel} from '../../models/mission-step-view-model';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';
import {FormBuilder} from '@angular/forms';

@Component({
  selector: 'app-mission-steps-card',
  templateUrl: './mission-steps-card.component.html',
  styleUrls: ['./mission-steps-card.component.css']
})
export class MissionStepsCardComponent implements OnInit {
  @Input() mission: Mission;

  constructor(private formBuilder: FormBuilder) { }

  stepForm = this.formBuilder.group({})

  drop(event: CdkDragDrop<string[]>) {
    console.log(this.mission.steps);
    moveItemInArray(this.mission.steps, event.previousIndex, event.currentIndex);
  }

  ngOnInit() {
    if(!this.mission.steps) {
      this.mission.steps = [new MissionStepViewModel()];
    }
    /*for(let i = 0; i < 5; i++) {
      this.mission.steps.push(new MissionStepViewModel());
      const newStep = this.mission.steps[i];
      newStep.count = 1;
      newStep.description = "Description of step " + i;
      newStep.displayName = "Step " + i;
    }*/
  }
}
