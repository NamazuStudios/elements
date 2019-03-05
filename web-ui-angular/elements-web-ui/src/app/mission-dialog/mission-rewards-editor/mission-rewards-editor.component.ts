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

  public rewardForm = this.formBuilder.group({
    newRewardItem: ['', [Validators.required], [this.itemExistsValidator.validate]],
    newRewardCt: ['', [Validators.required, Validators.pattern('^[0-9]+$')]]
  });

  public addReward(itemName: string, itemCt: number) {
    // block request if form not valid
    if (!this.rewardForm.valid) { return; }

    // get item specified by form
    this.itemsService.getItemByIdentifier(itemName).subscribe((item: Item) => {
      // add formControl
      this.rewardForm.addControl('reward' + this.rewards.length + 'Item',
        new FormControl('', [Validators.required], [this.itemExistsValidator.validate]));
      this.rewardForm.addControl('reward' + this.rewards.length + 'Ct',
        new FormControl('', [Validators.required, Validators.pattern('^[0-9]+$')]));

      // add to rewards item-array
      this.rewards.push({
        item: item,
        quantity: itemCt
      });

      // focus or blur on new name field?
      this.newItemField.nativeElement.focus();
      this.newItemField.nativeElement.blur();

      // clear form fields
      this.rewardForm.reset();
    });
  }

  ngOnInit() {
    this.rewards = this.rewards || [];
    for (let i = 0; i < this.rewards.length; i++) {
      this.rewardForm.addControl('reward' + i + 'Item', new FormControl('', [Validators.required], [this.itemExistsValidator.validate]));
      this.rewardForm.addControl('reward' + i + 'Ct', new FormControl('', [Validators.required, Validators.pattern('^[0-9]+$')]));
    }
  }

}
