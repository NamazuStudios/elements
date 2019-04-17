import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {Reward} from '../../api/models/reward';
import {FormBuilder, FormControl, Validators} from '@angular/forms';
import {ItemsService} from '../../api/services/items.service';
import {ItemExistsValidator} from '../../item-exists-validator';
import {Item} from '../../api/models/item';

@Component({
  selector: 'app-mission-rewards-editor',
  templateUrl: './mission-rewards-editor.component.html',
  styleUrls: ['./mission-rewards-editor.component.css']
})
export class BundleRewardsEditorComponent implements OnInit {
  @Input() rewards: Array<Reward>;
  @ViewChild('newRewardItem') newItemField: ElementRef;

  constructor(private formBuilder: FormBuilder, private itemsService: ItemsService) {}

  private itemExistsValidator = new ItemExistsValidator(this.itemsService);

  public newRewardForm = this.formBuilder.group({
    newRewardItem: ['', [], [this.itemExistsValidator.validate]],
    newRewardCt: ['', [Validators.required, Validators.pattern('^[0-9]+$')]]
  });

  public existingRewardForm = this.formBuilder.group({});

  public addReward(itemName: string, itemCt: number) {
    // block request if form not valid
    if (!this.newRewardForm.valid) { return; }

    // get item specified by form
    this.itemsService.getItemByIdentifier(itemName).subscribe((item: Item) => {
      // add to rewards item-array
      this.rewards.push({
        item: item,
        quantity: itemCt
      });

      // add formControl
      this.existingRewardForm.addControl('reward' + (this.rewards.length - 1) + 'Item',
        new FormControl(this.rewards[this.rewards.length - 1].item.name, [Validators.required], [this.itemExistsValidator.validate]));
      this.existingRewardForm.addControl('reward' + (this.rewards.length - 1) + 'Ct',
        new FormControl(this.rewards[this.rewards.length - 1].quantity, [Validators.required, Validators.pattern('^[0-9]+$')]));

      // focus or blur on new name field?
      this.newItemField.nativeElement.focus();

      // clear form fields
      this.newRewardForm.reset();
    });
  }

  removeReward(index: number) {
    if(this.rewards.length > 1) {
      this.rewards.splice(index, 1);
    } else {
      alert("At least one reward is required for each step. Add another reward before deleting this one.");
    }
  }

  updateReward(reward: Reward, param: string, event: any) {
    reward[param] = event.target.value;
  }

  ngOnInit() {
    this.rewards = this.rewards || [];
    for (let i = 0; i < this.rewards.length; i++) {
      this.existingRewardForm.addControl('reward' + i + 'Item', new FormControl(this.rewards[i].item.name, [Validators.required], [this.itemExistsValidator.validate]));
      this.existingRewardForm.addControl('reward' + i + 'Ct', new FormControl(this.rewards[i].quantity, [Validators.required, Validators.pattern('^[0-9]+$')]));
    }
  }

}
