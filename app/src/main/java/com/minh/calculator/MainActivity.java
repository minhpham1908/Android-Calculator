package com.minh.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    // empty nghia la moi bien co trang thai nhu sau.
    public boolean empty = true;
    String fullText = "0"; //chua bieu thuc
    String prevCalText = ""; //chua bieu thuc ket thuc la 1 phep toan
    String textText = "0"; //chi chua so
    public boolean isPreviousInputOperator = false;


    public void testState() {
        Log.v("input", "********************************************************");
        Log.v("input", "Full text: " + fullText);
        Log.v("input", "Prev cal text: " + prevCalText);
        Log.v("input", "Text text: " + textText);
        Log.v("input", "Empty?: " + empty);
        Log.v("input", "isPreviousInputOperator?: " + isPreviousInputOperator);
        Log.v("input", "********************************************************");
    }

    // phan tich cu phap
    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('×')) x *= parseFactor(); // multiplication
                    else if (eat('÷')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    public String negate(String text) {
        double number = eval(text);
        number = -number;
        String reverse = "";
        if (text.matches("-?\\d+(\\.0+)?")) {
            int i = (int) number;
            reverse = String.valueOf(i);
        } else {
            reverse = String.valueOf(number);
        }
        return reverse;
    }

    public static String calculate(String text) {
        String answer = String.valueOf(eval(text));
        if (answer.matches("-?\\d+\\.0+")) {
            int i = (int) eval(text);
            return String.valueOf(i);
        } else {
            return answer;
        }
    }

    public void onClickButton(String input, TextView text, TextView prevCal) {
        if (input.matches("\\d")) {
            if (!empty) {
                fullText = fullText.concat(input);
            } else if (empty) {
                fullText = input;
            }
            textText = fullText.substring(prevCalText.length());
            text.setText(textText);
            empty = false;
            isPreviousInputOperator = false;

        } else if (input.matches("[+\\-÷×]")) {
            Log.v("input", "op");
            if (empty) {
                textText = "0";
            } else if (!empty) {
                String lastChar = fullText.substring(fullText.length() - 1);
                if (lastChar.matches("\\d")) {
                    textText = calculate(fullText);
                } else if (lastChar.matches("[+\\-÷×]")) {
                    fullText = fullText.substring(0, fullText.length() - 1);

                }
            }
            fullText = fullText.concat(input);
            prevCalText = fullText;
            text.setText(textText);
            prevCal.setText(prevCalText);
            isPreviousInputOperator = true;
            empty = false;

        } else if (input.matches("C")) {
            //xoa fulltext, xoa preCal, xoa text
            Log.v("input", "C");
            fullText = "0";
            prevCalText = "";
            textText = "0";
            prevCal.setText(prevCalText);
            text.setText(textText);
            isPreviousInputOperator = false;
            empty = true;

        } else if (input.matches("\\.")) {
            Log.v("input", ".");
            if (empty) {
                fullText = fullText.concat(".");
                textText = fullText;

            } else if (isPreviousInputOperator == true) {
                fullText = fullText.concat("0.");
                textText = "0.";
                isPreviousInputOperator = false;
            }
            if (fullText.indexOf('.') < 0) {
                fullText = fullText.concat(input);
                textText = fullText;
            }
            text.setText(textText);
            empty = false;
        } else if (input.matches("CE")) {
            if (!empty) {
                fullText = fullText.substring(0, fullText.length() - textText.length());
                textText = "0";
                text.setText(textText);
            }

        } else if (input.matches("del")) {
            Log.v("input", "del");
            //xoa chu so cuoi cung cua full text den phep tinh
            // xoa chu so cuoi cung cua text, neu con 1 so -> ve 0
            if (isPreviousInputOperator == false) {
                if (textText.length() > 1) {
                    textText = textText.substring(0, textText.length() - 1);
                    fullText = fullText.substring(0, fullText.length() - 1);

                } else if (textText.length() == 1) {
                    textText = "0";
                    fullText = prevCalText;
                }


            } else if (isPreviousInputOperator == true) {
                //do nothing
            }

            text.setText(textText);
        } else if (input.matches("=")) {
            Log.v("input", "=");
            prevCalText = "";
            if (fullText.substring(fullText.length() - 1).matches("\\d"))
                fullText = calculate(fullText);
            else {
                fullText = calculate(fullText.concat(textText));
            }
            textText = fullText;
            prevCal.setText(prevCalText);
            text.setText(textText);
        } else if (input.matches("±")) {
            if (!empty) {
                if (isPreviousInputOperator == false) {
                    fullText = fullText.substring(0, fullText.length() - textText.length());
                    textText = negate(textText);
                    fullText = fullText.concat(textText);
                } else if (isPreviousInputOperator == true) {
                    textText = negate(textText);
                }
            }
            text.setText(textText);
            prevCal.setText(prevCalText);
        }

        testState();

    }


    public void setContext(final Button button, int pos) {
        final TextView text = (TextView) findViewById(R.id.editText);
        final TextView prevCal = (TextView) findViewById(R.id.prevCal);
        switch (pos) {
            case 0:
                button.setText("CE");
                button.setId(R.id.CE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 1:
                button.setText("C");
                button.setId(R.id.C);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 2:
                button.setText("del");
                button.setId(R.id.del);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if (!empty) {
//                            if (text.length() > 1) {
//                                text.setText(text.getText().toString().substring(0, text.length() - 1));
//                            } else if (text.length() == 1) {
//                                text.setText("0");
//                                empty = true;
//                            }
//                        }
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 3:
                button.setText("÷");
                button.setId(R.id.divide);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 10:
                button.setText("7");
                button.setId(R.id.seven);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 11:
                button.setText("8");
                button.setId(R.id.eight);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 12:
                button.setText("9");
                button.setId(R.id.nine);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 13:
                button.setText("×");
                button.setId(R.id.multiply);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 20:
                button.setText("4");
                button.setId(R.id.four);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 21:
                button.setText("5");
                button.setId(R.id.five);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 22:
                button.setText("6");
                button.setId(R.id.six);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 23:
                button.setText("-");
                button.setId(R.id.minus);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 30:
                button.setText("1");
                button.setId(R.id.one);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });

                break;
            case 31:
                button.setText("2");
                button.setId(R.id.two);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 32:
                button.setText("3");
                button.setId(R.id.three);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 33:
                button.setText("+");
                button.setId(R.id.plus);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("input", "+");
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 40:
                button.setText("±");
                button.setId(R.id.plusMinus);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 41:
                button.setText("0");
                button.setId(R.id.zero);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 42:
                button.setText(".");
                button.setId(R.id.dot);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
            case 43:
                button.setText("=");
                button.setId(R.id.equal);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickButton(button.getText().toString(), text, prevCal);
                    }
                });
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        LinearLayout root = (LinearLayout) findViewById(R.id.root);

        //Tao cac nut cho may tinh
        for (int row = 0; row < 5; row++) {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.1f);
            rowParams.setMargins(0, 0, 0, 0);
            linearLayout.setLayoutParams(rowParams);
            linearLayout.setWeightSum(4);
            for (int i = 0; i < 4; i++) {
                int pos = 10 * row + i;

//                if (pos == 2) {
//                    ImageButton b = new ImageButton(this);
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
//                    b.setId(R.id.del);
//
//                    b.setImageResource(R.drawable.outline_backspace_24);
//                    b.setLayoutParams(params);
//                    linearLayout.addView(b);
//                } else {
                Button b = new Button(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                setContext(b, 10 * row + i);
                b.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                b.setLayoutParams(params);
                linearLayout.addView(b);
//                }


            }
            root.addView(linearLayout);

        }
        Log.v("input", "Fulltext: " + fullText);
        Log.v("input", "prevCalText: " + prevCalText);
        Log.v("input", "textText: " + textText);
        Log.v("input", empty ? "true" : "false");

    }
}
