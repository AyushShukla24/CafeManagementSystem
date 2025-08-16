import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../service/user.service';
import { MatDialogRef } from '@angular/material/dialog';
import { NgxUiLoaderComponent } from 'ngx-ui-loader/lib/core/ngx-ui-loader.component';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { SnackbarService } from '../service/snackbar.service';
import { GlobalConstants } from '../shared/global-constants';

@Component({
  selector: 'app-forget-password',
  templateUrl: './forget-password.component.html',
  styleUrls: ['./forget-password.component.scss']
})
export class ForgetPasswordComponent implements OnInit {

  forgetPasswordForm:any = FormGroup;

  constructor(private formBuilder: FormBuilder, 
              private userService: UserService, 
              public dialogRef: MatDialogRef<ForgetPasswordComponent>, 
              private ngxService: NgxUiLoaderService, 
              private snackBarService: SnackbarService) { }

  ngOnInit(): void {
    this.forgetPasswordForm = this.formBuilder.group({
      email: [null, [Validators.required, Validators.pattern(GlobalConstants.emailRegex)]]
  });
}

  handleSubmit() {
    this.ngxService.start();
    const formData = this.forgetPasswordForm.value;
    const data = {
      email: formData.email
    };

    let responseMessage: string;

    this.userService.forgetPassword(data).subscribe((response: any) => {
      this.ngxService.stop();
      this.dialogRef.close();
      responseMessage = response?.message;
      this.snackBarService.openSnackBar(responseMessage, '');
    }, (error: any) => {
      this.ngxService.stop();
      if(error?.error?.message){
        responseMessage = error.error.message;
      } else {
        responseMessage = GlobalConstants.genericError;
      }
      this.snackBarService.openSnackBar(responseMessage, GlobalConstants.error);
    });
  }

}
