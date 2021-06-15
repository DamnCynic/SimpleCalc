package com.example.simplecalc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static java.lang.Math.pow;
import static java.lang.Math.round;

public class MainActivity extends AppCompatActivity {
    public static final int opCodeSummarize = 1;        //Codes representing arithmetical operations
    public static final int opCodeSubtraction = 2;
    public static final int opCodeMultiply = 3;
    public static final int opCodeDivide = 4;
    public static final int opCodeElevate = 5;

    public static final int screenReady = 0;            //Codes used for screen governing (incl. blocking)
    public static final int screenAppending = 1;
    public static final int screenBlocked = 2;

    public static final int systemStateTextEntry = 1;   //Codes used for process staging (i.e. text entry,
    public static final int systemStateOperationSelect = 2; // arithmetic op. choosing,
    public static final int systemStateResultPressed = 4;   // invoking of "=" (equity) op.)

    protected int screenManagementFlag = 0;             //Flag variables
    protected int systemStateFlag = 0;
    protected int operationCode = 0;
    protected double argumentFirst = 0, argumentSecond = 0, memoryCell = 0;
    protected boolean FPSet = false;                    //On-screen floating point management (is already set or not)
    protected boolean memoryClear = false;              //management for R-CM combined (ReCall/ClearMem) button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onMyButtonClick(View view)
    {
        if (screenManagementFlag == screenBlocked) {
            if (view.getId() == R.id.buttonClear) {
                clearAll();
            }
            if (view.getId() == R.id.buttonOff) {
                exOff();
            }
            return;
        }
        if (memoryClear & (view.getId() == R.id.buttonRCM)) {
            memRecallClear();
            return;
        }
        memoryClear = false;

        switch(view.getId()) {
            case R.id.button0:
                charEntered("0");
                break;
            case R.id.button1:
                charEntered("1");
                break;
            case R.id.button2:
                charEntered("2");
                break;
            case R.id.button3:
                charEntered("3");
                break;
            case R.id.button4:
                charEntered("4");
                break;
            case R.id.button5:
                charEntered("5");
                break;
            case R.id.button6:
                charEntered("6");
                break;
            case R.id.button7:
                charEntered("7");
                break;
            case R.id.button8:
                charEntered("8");
                break;
            case R.id.button9:
                charEntered("9");
                break;
            case R.id.buttonFloatingPoint:
                setFP();
                break;
            case R.id.buttonExponent:
                operationSelecting(opCodeElevate);
                break;
            case R.id.buttonCE:
                clearCE();
                break;
            case R.id.buttonClear:
                clearAll();
                break;
            case R.id.buttonDivide:
                operationSelecting(opCodeDivide);
                break;
            case R.id.buttonMultiply:
                operationSelecting(opCodeMultiply);
                break;
            case R.id.buttonPlus:
                operationSelecting(opCodeSummarize);
                break;
            case R.id.buttonMinus:
                operationSelecting(opCodeSubtraction);
                break;
            case R.id.buttonInvert:
                getScreenInvert();;
                break;
            case R.id.buttonEqual:
                getEquity();
                break;
            case R.id.buttonMPlus:
                memSetAdd();
                break;
            case R.id.buttonMMinus:
                memSetSubtract();
                break;
            case R.id.buttonRCM:
                memRecallClear();
                break;
            case R.id.buttonOff:
                exOff();
                break;
        }
    }

    protected void exOff() {
        this.finishAffinity();
        System.exit(0);
    }

    protected void charEntered(String num) {
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        if (systemStateFlag == systemStateResultPressed) {
            //If we pressed a digit after computing (finalized by '='),
            // then we begin new computing cycle
            operationCode = 0;
        }
        systemStateFlag = systemStateTextEntry;
        if (screenManagementFlag == screenBlocked)
            return;
        if (screenManagementFlag == screenReady) {
            if (num.toString().equals("."))
                editTextNumber.setText("0" + num);
            else
                editTextNumber.setText(num);
            screenManagementFlag = screenAppending;
        }
        else if (screenManagementFlag == screenAppending)
            editTextNumber.append(num);
    }

    protected void screenWrite(double arg) {
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        if (arg % 1.0 != 0)
            editTextNumber.setText(String.format("%s", arg));
        else
            editTextNumber.setText(String.format("%.0f", arg));
    }

    protected void operationSelecting(int opCode){
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        if (systemStateFlag == systemStateTextEntry) {
            if (operationCode == 0) {
                argumentFirst = Double.parseDouble(editTextNumber.getText().toString());
                argumentSecond = argumentFirst;     //if user would press '='
            }
            else {
                argumentSecond = Double.parseDouble(editTextNumber.getText().toString());
                argumentFirst = getCompute(operationCode);
                if (screenManagementFlag == screenBlocked)      //divide by 0
                    return;
                screenWrite(argumentFirst);
            }
        }
        else if (systemStateFlag == systemStateResultPressed) {
            argumentFirst = Double.parseDouble(editTextNumber.getText().toString());
            //arg2 without changes
        }
        systemStateFlag = systemStateOperationSelect;
        operationCode = opCode;
        screenManagementFlag = screenReady;
        FPSet = false;
    }

    protected void getEquity() {
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        if (systemStateFlag == systemStateTextEntry) {
            if (operationCode != 0) {
                //Regular condition 'a + b = c'.
                argumentSecond = Double.parseDouble(editTextNumber.getText().toString());
                argumentFirst = getCompute(operationCode);
                if (screenManagementFlag == screenBlocked)      //divide by 0
                    return;
                screenWrite(argumentFirst);
            }
        }
        else {
            // After 'a ? b = c' set a = c and repeat;
            argumentFirst = Double.parseDouble(editTextNumber.getText().toString());
            screenWrite(getCompute(operationCode));
            if (screenManagementFlag == screenBlocked)      //divide by 0
                return;
        }
        systemStateFlag = systemStateResultPressed;
        screenManagementFlag = screenReady;
        FPSet = false;
    }

    protected double getCompute(int opCode) {
        double result = 0;
        double Digit;
        Digit = pow(10,12);
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        switch (opCode) {
            case opCodeSummarize:
                result = argumentFirst + argumentSecond;
                break;
            case opCodeSubtraction:
                result = argumentFirst - argumentSecond;
                break;
            case opCodeMultiply:
                result = argumentFirst * argumentSecond;
                break;
            case opCodeDivide:
                if (argumentSecond != 0)
                    result = argumentFirst / argumentSecond;
                else {
                    clearAll();
                    editTextNumber.setText("E");
                    screenManagementFlag = screenBlocked;
                    return 0;
                }
                break;
            case opCodeElevate:
                result = pow(argumentFirst, argumentSecond);
                return result;
            default:
                result = Double.parseDouble(editTextNumber.getText().toString());
        }
        result = result * Digit;        //Rounding 'double' to avoid transition errors
        result = round(result);
        result = result / Digit;
        return result;
    }

    protected void getScreenInvert() {
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        double c;
        c = Double.parseDouble(editTextNumber.getText().toString());
        c *= -1;
        screenWrite(c);
    }

    protected void setFP() {
        if (!FPSet) {
            FPSet = true;
            charEntered(".");
        }
    }

    protected void clearCE() {
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        if (!(systemStateFlag == systemStateTextEntry)) {
            screenManagementFlag = 0;
            systemStateFlag = 0;
            operationCode = 0;
            argumentFirst = 0; argumentSecond = 0;
        }
        screenWrite(0);
        FPSet = false;
        screenManagementFlag = screenReady;
    }

    protected void clearAll() {
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        TextView memTextView = (TextView) findViewById(R.id.textViewMemory);
        editTextNumber.setText("0");
        memTextView.setText("");
        screenManagementFlag = 0;
        systemStateFlag = 0;
        operationCode = 0;
        argumentFirst = 0; argumentSecond = 0; memoryCell = 0;
        FPSet = false;
        memoryClear = false;
    }

    protected void memSetAdd() {
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        TextView memTextView = (TextView) findViewById(R.id.textViewMemory);
        memoryCell += Double.parseDouble(editTextNumber.getText().toString());
        if (memoryCell != 0) memTextView.setText("M");
        else memTextView.setText("");
    }
    protected void memSetSubtract() {
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        TextView memTextView = (TextView) findViewById(R.id.textViewMemory);
        memoryCell -= Double.parseDouble(editTextNumber.getText().toString());
        if (memoryCell != 0) memTextView.setText("M");
        else memTextView.setText("");
    }
    protected void memRecallClear() {
        EditText editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        TextView memTextView = (TextView) findViewById(R.id.textViewMemory);
        if (!memoryClear) {
            screenWrite(memoryCell);
            memoryClear = true;
        }
        else {
            memoryCell = 0;
            memTextView.setText("");
        }
    }
}