import { Component, Input, OnInit } from '@angular/core';
import { NeoWalletsService } from '../../api/services';
import { NeoSmartContractsService } from '../../api/services/blockchain/neo-smart-contracts.service';
import { NeoTokensService } from '../../api/services/blockchain/neo-tokens.service';

import { UserLevel } from "../../users/user-dialog/user-dialog.component";

@Component({
  selector: 'app-blockchain-dropdown',
  templateUrl: './blockchain-dropdown.component.html',
  styleUrls: ['./blockchain-dropdown.component.css']
})
export class BlockchainDropdownComponent implements OnInit {

  @Input() serviceType = "token";
  currentNetwork = "NEO";

  blockchainOptions: UserLevel[] = [
    { key: "NEO", description: "NEO" },
    { key: "BSC", description: "BSC" },
  ];

  constructor(
    private neoTokensService: NeoTokensService,
    private neoSmartContractsService: NeoSmartContractsService,
    private neoWalletsService: NeoWalletsService
  ) { }

  ngOnInit(): void {
    switch(this.serviceType) {
      case "smart":
        this.neoSmartContractsService.network.subscribe(
          (network: string) => (this.currentNetwork = network)
        );
        break;
      case "wallet":
        this.neoWalletsService.network.subscribe(
          (network: string) => (this.currentNetwork = network)
        );
        break;
      default:
        this.neoTokensService.network.subscribe(
          (network: string) => (this.currentNetwork = network)
        );
        break;
    }
  }

  // update network
  changeNetwork(event) {
    switch(this.serviceType) {
      case "smart":
        this.neoSmartContractsService.network.next(event.value);
        break;
      case "wallet":
        this.neoWalletsService.network.next(event.value);
        break;
      default:
        this.neoTokensService.network.next(event.value);
        break;
    }
  }
}
