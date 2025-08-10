import { Component } from '@angular/core';
import { NgxUiLoaderModule } from 'ngx-ui-loader';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'Frontend';
}


// to start project:-> cmd /c "set NODE_OPTIONS=--openssl-legacy-provider && ng serve"