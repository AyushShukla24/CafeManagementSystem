import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ForgetPasswordComponent } from '../forget-password/forget-password.component';
import { MatDialogRef } from '@angular/material/dialog';
import { UserService } from '../service/user.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { SnackbarService } from '../service/snackbar.service';
import { GlobalConstants } from '../shared/global-constants';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  hide = true;
  loginForm:any = FormData;
  responseMessage: any;

   constructor(private formBuilder: FormBuilder, 
               private userService: UserService, 
               public dialogRef: MatDialogRef<ForgetPasswordComponent>, 
               private ngxService: NgxUiLoaderService, 
               private snackBarService: SnackbarService,
               private router: Router
              ) { }
 
   ngOnInit(): void {
     this.loginForm = this.formBuilder.group({
       email: [null, [Validators.required, Validators.pattern(GlobalConstants.emailRegex)]],
       password: [null, [Validators.required]]
   });
 }

 handleSubmit() {
  this.ngxService.start(); 
  var formData = this.loginForm.value;
  var data = {
    email: formData.email, 
    password: formData.password
  }

  this.userService.login(data).subscribe((response: any) => {
  this.ngxService.stop();
  this.dialogRef.close(); 
  localStorage.setItem('token', response.token); 
  this.router.navigate(['/cafe/dashboard']);
}, (error) => {
  if (error.error?.message) {
    this.responseMessage = error.error?.message; 
  } else { 
    this.responseMessage = error?.message || GlobalConstants.genericError;
  }
  this.ngxService.stop();

  this.snackBarService.openSnackBar(this.responseMessage, GlobalConstants.error);
});

}
}
