import { Pipe, PipeTransform } from "@angular/core";
import { OptionType } from "../neo-token-dialog/neo-token-dialog.component";

@Pipe({
  name: "transferOptions",
})
export class TransferOptionsPipe implements PipeTransform {
  transferOptionType: OptionType[] = [
    { key: "none", label: "N", toolTip: "Cannot be transferred." },
    {
      key: "resale_only",
      label: "R",
      toolTip: "Can be resold, but not traded.",
    },
    {
      key: "trades_only",
      label: "T",
      toolTip: "Can be traded, but not resold.",
    },
    {
      key: "resale_and_trades",
      label: "R & T",
      toolTip: "Can be either resold or traded.",
    },
  ];
  
  transform(key: string, option: string): string {
    const data: OptionType[] = this.transferOptionType.filter(option => option.key === key);
    if(!data[0]){
      return "";
    }

    if(option === "label"){
      return data[0]?.label;
    } else if (option === "toolTip") {
      return data[0]?.toolTip;
    }
    
  }
}
