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
export class MissionRewardsEditorComponent implements OnInit {
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
    console.log("Adding reward");
    // block request if form not valid
    if (!this.newRewardForm.valid) { return console.log("Rewardform invalid"); }
    console.log("Reward valid");

    // get item specified by form
    this.itemsService.getItemByIdentifier(itemName).subscribe((item: Item) => {
      console.log("Found item");
      // add formControl
      this.existingRewardForm.addControl('reward' + this.rewards.length + 'Item',
        new FormControl('', [Validators.required], [this.itemExistsValidator.validate]));
      this.existingRewardForm.addControl('reward' + this.rewards.length + 'Ct',
        new FormControl('', [Validators.required, Validators.pattern('^[0-9]+$')]));

      console.log("Added next form control");

      // add to rewards item-array
      this.rewards.push({
        item: item,
        quantity: itemCt
      });

      console.log("Attached to rewards item-array");

      // focus or blur on new name field?
      this.newItemField.nativeElement.focus();

      console.log("reset focus");

      // clear form fields
      this.newRewardForm.reset();

      console.log("Reset form fields");
    });
  }

  removeReward(index: number) {
    this.rewards.splice(index, 1);
  }

  ngOnInit() {
    this.rewards = this.rewards || [];
    for (let i = 0; i < this.rewards.length; i++) {
      //this.existingRewardForm.addControl('reward' + i + 'Item', new FormControl('', [Validators.required], [this.itemExistsValidator.validate]));
      //this.existingRewardForm.addControl('reward' + i + 'Ct', new FormControl('', [Validators.required, Validators.pattern('^[0-9]+$')]));
      this.existingRewardForm.addControl('reward' + i + 'Item', new FormControl('', [Validators.required], [this.itemExistsValidator.validate]));
      this.existingRewardForm.addControl('reward' + i + 'Ct', new FormControl('', [Validators.required, Validators.pattern('^[0-9]+$')]));
    }
  }

}
