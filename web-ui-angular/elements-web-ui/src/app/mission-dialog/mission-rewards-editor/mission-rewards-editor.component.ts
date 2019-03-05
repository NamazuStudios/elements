import {Component, Input, OnInit} from '@angular/core';
import {Reward} from '../../api/models/reward';
import {FormBuilder, FormControl, Validators} from '@angular/forms';
import {itemExistsValidator} from '../../item-exists.directive';
import {ItemsService} from '../../api/services/items.service';

@Component({
  selector: 'app-mission-rewards-editor',
  templateUrl: './mission-rewards-editor.component.html',
  styleUrls: ['./mission-rewards-editor.component.css']
})
export class MissionRewardsEditorComponent implements OnInit {
  @Input() rewards: Array<Reward>;

  constructor(private formBuilder: FormBuilder, private itemsService: ItemsService) { }

  public rewardForm = this.formBuilder.group({
    newRewardItem: ['', [Validators.required], [itemExistsValidator(this.itemsService)]],
    newRewardCt: ['', [Validators.required]]
  });

  // TODO check validity of item name, attach to rewards array, clear form fields
  public addReward(itemName: String, itemCt: number) {
    // check if form valid
  }

  ngOnInit() {
    this.rewards = this.rewards || [];
    for(let i = 0; i < this.rewards.length; i++) {
      this.rewardForm.addControl('reward' + i + 'Item', new FormControl('', []));
      this.rewardForm.addControl('reward' + i + 'Ct', new FormControl('', []));
    }
  }

}
