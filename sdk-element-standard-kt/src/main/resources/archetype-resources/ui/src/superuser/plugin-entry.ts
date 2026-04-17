import { ExamplePlugin } from './ExamplePlugin'

declare const window: Window & {
  __elementsPlugins?: {
    register(route: string, component: unknown): void
  }
}

window.__elementsPlugins?.register('example-element', ExamplePlugin)
