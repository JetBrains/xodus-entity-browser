import {ChangeEvent, Component} from 'react';

export abstract class FormSupport<Props> extends Component<Props> {

  abstract data(): any;

  get readOnly(): boolean {
    return false;
  }

  checkboxHandler(name: string): () => void {
    if (this.readOnly) {
      return () => {
      };
    }
    const getter = this.data.bind(this);
    return () => {
      const data = getter();
      // @ts-ignore
      data[name] = !data[name];
    };
  }

  inputHandler(name: string, isNumber = false) {
    const getter = this.data.bind(this);
    return (event: ChangeEvent<HTMLInputElement>) => {
      const data = getter();
      // @ts-ignore
      data[name] = isNumber ? Number(event.target.value) : event.target.value;
    };
  }
}
