package com.example.ToYokoNa.controller;

import com.example.ToYokoNa.controller.form.UserForm;
import com.example.ToYokoNa.repository.entity.User;
import com.example.ToYokoNa.service.UserService;
import com.example.ToYokoNa.utils.CipherUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    private HttpSession session;
    // ログイン画面表示
    @GetMapping("/userLogin")
    public ModelAndView login() {
        ModelAndView mav = new ModelAndView();
        UserForm userForm = new UserForm();
        // 遷移先
        mav.setViewName("userLogin");
        // 空のフォームの送信
        mav.addObject("userForm", userForm);
        return mav;
    }
    /*
     * ユーザーログイン処理
     */
    @PostMapping("/userLogin")
    public ModelAndView login(@ModelAttribute("userForm") @Validated UserForm userForm,
                              BindingResult bindingResult) {
        ModelAndView mav = new ModelAndView();
        if (bindingResult.hasErrors()) {
            return new ModelAndView("/userLogin");
        }
        // パスワードを暗号化
        String encPassword = CipherUtil.encrypt(userForm.getPassword());
        // 暗号化した物をuserFormにセット
        userForm.setPassword(encPassword);
        // try文の中と処理後にもuserDataオブジェクトを使用できるように先に作成
        UserForm userData;
        try {
            userData = userService.selectUser(userForm);
            // もしユーザが停止している場合,もしくはユーザ情報が存在しない場合
            if (userData.getIsStopped() == 1) {
                // 例外を投げてcatchで処理をする
                throw new Exception("ログインに失敗しました");
            }
        } catch (Exception e) {
            // 設定したメッセージを取得しerrorMessageとしてhtmlに渡す
            mav.addObject("errorMessage", e.getMessage());
            mav.setViewName("/userLogin");
            return mav;
        }
        // セッションに値をセット
        session.setAttribute("loginUser", userData);
        // topにリダイレクト
        mav.setViewName("redirect:/");
        return mav;
    }

    /*
     * ユーザー管理画面表示(ユーザー全取得)
     */
    @GetMapping("/userManage")
    public ModelAndView userManage() {
        ModelAndView mav = new ModelAndView();
        List<UserForm> users = userService.findAllUser();
        mav.setViewName("/userManage");
        mav.addObject("users", users);
        return mav;
    }

    /*
     * ユーザー復活・停止機能
     */
    @PutMapping("/{id}")
    public ModelAndView changeIsStopped(@PathVariable("id") int id) {
        // 対象のユーザを取得
        UserForm userData = userService.findById(id);
        // ユーザのisStoppedの値を変更させる
        if (userData.getIsStopped() == 1) {
            userData.setIsStopped(0);
        } else {
            userData.setIsStopped(1);
        }
        // 値を変更させたらデータを保存させる
        userService.saveUser(userData);
        return new ModelAndView("redirect:/userManage");
    }
}