package sicxe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

//*Dalia Abdelraouf-19102573/ Menna Tarek-19102091*//
public class SICXE {

    static List<String> labels = new ArrayList<>();
    static List<String> instructions = new ArrayList<>();
    static List<String> reference = new ArrayList<>();
    static List<String> loc_ctr = new ArrayList<>();
    static List<String> obj_code = new ArrayList<>();
    //symbol table
    static List<String> symbol = new ArrayList<>();  //labels
    static List<String> address = new ArrayList<>();  //loc ctr
    //OPTAB lists
    static List<String> opInst = new ArrayList<>();
    static List<String> opFormat = new ArrayList<>();
    static List<String> opCode = new ArrayList<>();
    //
    static List<String> T = new ArrayList<>();
    //Registers lists
    static List<String> register = new ArrayList<>();
    static List<String> registervalue = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {
        register.add("A");
        register.add("X");
        register.add("L");
        register.add("B");
        register.add("S");
        register.add("T");
        register.add("F");
        register.add("PC");
        register.add("SW");
        registervalue.add("0");
        registervalue.add("1");
        registervalue.add("2");
        registervalue.add("3");
        registervalue.add("4");
        registervalue.add("5");
        registervalue.add("6");
        registervalue.add("8");
        registervalue.add("9");
        try {
            readCodeFile();
            readConverterFile();
            LocationCounter();
            ObjectCode();

            //printing process
            for (int i = 0; i < instructions.size(); i++) {
                if (reference.get(i).endsWith(",X") || reference.get(i).endsWith(",f")) {
                    System.out.println(i + "\t" + loc_ctr.get(i) + "\t" + labels.get(i) + "\t" + instructions.get(i) + "\t" + reference.get(i) + " " + obj_code.get(i));
                } else {
                    System.out.println(i + "\t" + loc_ctr.get(i) + "\t" + labels.get(i) + "\t" + instructions.get(i) + "\t" + reference.get(i) + "\t " + obj_code.get(i));
                }

            }
            SymTable();
            // symbol table print
            System.out.println("\nSYMBOL TABLE:");
            for (int j = 0; j < symbol.size(); j++) {
                System.out.println(symbol.get(j) + "\t" + address.get(j));
            }
            HTE();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR!!");
        }
    }

    public static void readCodeFile() throws FileNotFoundException {
        //load the file
        File SicFile = new File("inSICXE.txt");
        //reading the file using scanner
        Scanner reader = new Scanner(SicFile);
        String data;
        //while file still has lines..
        while (reader.hasNextLine()) {
            data = reader.nextLine();
            // the "\\s+" is to escape all kinds of spaces whether tabs or whitespaces
            switch (data.split("\\s+").length) {
                case 1:
                    labels.add("");
                    instructions.add(data.split("\\s+")[0]);
                    reference.add("");
                    break;
                case 2:
                    labels.add("");
                    instructions.add(data.split("\\s+")[0]);
                    reference.add(data.split("\\s+")[1]);
                    break;
                case 3:
                    labels.add(data.split("\\s+")[0]);
                    instructions.add(data.split("\\s+")[1]);
                    reference.add(data.split("\\s+")[2]);
                    break;
                default:
                    break;
            }
        }
        reader.close();
    }

    public static void readConverterFile() throws FileNotFoundException {
        //load the file
        File ConvertFile = new File("converter.txt");
        //reading the file using scanner
        Scanner read = new Scanner(ConvertFile);
        String converterData;
        //READ CONVERTER FILE
        while (read.hasNextLine()) {
            converterData = read.nextLine();
            opInst.add(converterData.split(",")[0]);
            opFormat.add(converterData.split(",")[1]);
            opCode.add(converterData.split(",")[2]);
        }

        read.close();
    }

//<----------------LOCATION COUNTER:--------------------------------------------------------------------------------------->
    public static void LocationCounter() throws FileNotFoundException {
        //set digit's formats to 4-digit number->"0000"
        String f = reference.get(0);
        String digitform = String.format("%4s", f).replace(' ', '0');
        loc_ctr.add(digitform);
        loc_ctr.add(digitform);
        String tempMem = reference.get(0);
        //now checkig what to do in each of the following cases (RESW,RESB,BYTE X'', BYTE C'')
        for (int j = 1; j < instructions.size() - 1; j++) {
            //checks whether there are any empty elements in the arraylist to replace them with hashtags
            if (labels.get(j).isEmpty()) {
                labels.set(j, "////");
                labels.set(labels.size() - 1, "////");
            }
            if (instructions.get(j).isEmpty()) {
                instructions.set(j, "////");
            }
            if (reference.get(j).isEmpty()) {
                reference.set(j, "////");
            }
            //if instructions has RESW multiply by 3, convert to hex, then add to array
            if (instructions.get(j).equalsIgnoreCase("RESW")) {
                int multiply = Integer.parseInt(reference.get(j)) * 3;
                int addition = Integer.parseInt(tempMem, 16) + multiply;// convert hex string to decimal + the multiplication process
                tempMem = Integer.toHexString(addition);
                String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                loc_ctr.add(digitformat);
            } //if instructions has RESB add decimals, convert to hex, then add to array
            else if (instructions.get(j).equalsIgnoreCase("RESB")) {
                int add = Integer.parseInt(tempMem, 16) + Integer.parseInt(reference.get(j));
                tempMem = Integer.toHexString(add);
                String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                loc_ctr.add(digitformat);
            }//if instructions has BYTE 
            else if (instructions.get(j).equalsIgnoreCase("BYTE")) {
                //if it starts with X count each 2 hex as a 1 byte, then add to array
                if (reference.get(j).startsWith("X")) {
                    String temp = reference.get(j);
                    int byteX = (temp.length() - 3) / 2; // -3 ly heya C''
                    int add = Integer.parseInt(tempMem, 16) + byteX;
                    tempMem = Integer.toHexString(add);
                    String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                    loc_ctr.add(digitformat);
                }// if it starts with C count length then add to array
                else if (reference.get(j).startsWith("C")) {
                    String temp = reference.get(j);
                    int byteC = temp.length() - 3; // -3 ly heya C''
                    int add = Integer.parseInt(tempMem, 16) + byteC;
                    tempMem = Integer.toHexString(add);
                    String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                    loc_ctr.add(digitformat);
                }
            }//else if instruction has Base, do nothing just add the previous location counter 
            else if (instructions.get(j).equalsIgnoreCase("BASE")) {
                String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                loc_ctr.add(digitformat);
            } else if (instructions.get(j).equalsIgnoreCase("WORD")) {
                if (reference.get(j).contains(",")) {
                    List<String> tmpArray = new ArrayList<>();
                    tmpArray.addAll(Arrays.asList(reference.get(j).split(",")));
                    int add = Integer.parseInt(tempMem, 16) + tmpArray.size() * 3;
                    tempMem = Integer.toHexString(add);
                    String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                    loc_ctr.add(digitformat);
                }
            } //if it starts with a + then it is format 4, add 4 to the location counter 
            else if (instructions.get(j).startsWith("+")) {
                int addFour = Integer.parseInt(tempMem, 16) + 4; // convert hex string to decimal
                tempMem = Integer.toHexString(addFour);
                String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                loc_ctr.add(digitformat);
            } else if (instructions.get(j).equalsIgnoreCase("REGF")) {
                loc_ctr.add(tempMem);
            } //
            else if (instructions.get(j).startsWith("&")) {
                int addFive = Integer.parseInt(tempMem, 16) + 6; // convert hex string to decimal
                tempMem = Integer.toHexString(addFive);
                String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                loc_ctr.add(digitformat);
            } else {
                int formatIndex = opInst.indexOf(instructions.get(j));
                String format = opFormat.get(formatIndex);
                switch (format) {
                    //if the instructions has format 1 add 1
                    case "1": {
                        int addOne = Integer.parseInt(tempMem, 16) + 1; // convert hex string to decimal
                        tempMem = Integer.toHexString(addOne);
                        String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                        loc_ctr.add(digitformat);
                        break;
                    }
                    //if the instructions has format 2 add 2
                    case "2": {
                        int addTwo = Integer.parseInt(tempMem, 16) + 2; // convert hex string to decimal
                        tempMem = Integer.toHexString(addTwo);
                        String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                        loc_ctr.add(digitformat);
                        break;
                    }//else add 3 (format 3)
                    default: {
                        //initiate a temp to make the addition process, then add it to the loc_ctr array
                        int addThree = Integer.parseInt(tempMem, 16) + 3; // convert hex string to decimal
                        tempMem = Integer.toHexString(addThree);
                        String digitformat = String.format("%4s", tempMem).replace(' ', '0');
                        loc_ctr.add(digitformat);
                        break;
                    }
                }

            }
        }
    }

    //<-----SYMBOL TABLE---------------------------------------------------------------------------------------------------------------->
    public static void SymTable() {
        for (int j = 1; j < labels.size(); j++) {
            if (!labels.get(j).matches("////")) {
                symbol.add(labels.get(j));
                address.add(loc_ctr.get(j));
            }
        }
    }

    //<-----OBJECT CODE---------------------------------------------------------------------------------------------------------------->
    static int n = 0;
    static int i = 0;
    static int x = 0;
    static int b = 0;
    static int p = 0;
    static int e = 0;
    static String disp = "";
    static String addr = "";
    static String op1 = "";
    static String op2 = "";
    static String dfOp2 = "";
    static String binary1 = "";
    static String binary2 = "";
    static String opni = "";
    static String xbpe = "";
    static int bin1 = 0;
    static int bin2 = 0;
    static String opnihex = "";
    static String xbpehex = "";
    static String op1hex = "";
    static String obj = "";

    public static void ObjectCode() {
        obj_code.add("------");
        SymTable();
        for (int j = 1; j < instructions.size(); j++) {
            //if instructions has RESW, END,RESB or BASE don't add object code
            if (instructions.get(j).equalsIgnoreCase("RESW") || instructions.get(j).equalsIgnoreCase("RESB")
                    || instructions.get(j).equalsIgnoreCase("BASE") || instructions.get(j).equalsIgnoreCase("END")) {
                obj_code.add("------");
            } //if instructions has WORD convert ref to hex, then add to object code
            else if (instructions.get(j).equalsIgnoreCase("WORD")) {
                String hex = Integer.toHexString(Integer.parseInt(reference.get(j)));//get the hex string of the reference
                int c = Integer.parseInt(hex);//then format it as integer
                obj_code.add(String.format("%06d", c));
            } //if instructions has BYTE 
            else if (instructions.get(j).equalsIgnoreCase("BYTE")) {
                //if it starts with X, add it to object code as it is
                if (reference.get(j).startsWith("X")) {
                    String temp = reference.get(j);
                    obj_code.add(temp.substring(2, temp.length() - 1));
                }// if it starts with C, get ASCII code, then add to object code array
                else if (reference.get(j).startsWith("C")) {
                    String temp = reference.get(j);//temp that stores the reference
                    String split = temp.substring(2, temp.length() - 1);//gets the substring bteween C''
                    String ascii = "";
                    for (int k = 0; k < split.length(); k++) {
                        ascii += Integer.toHexString((int) split.charAt(k));//gets the Hex string of the integer ASCII code
                    }
                    obj_code.add(ascii);
                }
            } else if (instructions.get(j).equalsIgnoreCase("REGF")) {
                obj_code.add("------");
            } else if (instructions.get(j).startsWith("+")) {
                getBinaryOp(j);
                e = 1;
                x = 0;
                b = 0;
                p = 0;
                if (reference.get(j).startsWith("#")) {
                    n = 0;
                    i = 1;
                    if (reference.get(j).substring(1).matches("\\d+")) {
                        int intref = Integer.parseInt(reference.get(j).substring(1));
                        addr = Integer.toHexString(intref);
                        String digitformat = String.format("%5s", addr).replace(' ', '0');
                        getHexOp2nixbpe(n, i, x, b, p, e);
                        obj = op1 + opnihex + xbpehex + digitformat;
                        obj_code.add(obj);
                    } //
                    else if (reference.get(j).substring(1).matches("\\w+")) {
                        getObjofF4(j);
                    }
                } else if (reference.get(j).startsWith("@")) {
                    n = 1;
                    i = 0;
                    x = 0;
                    getObjofF4(j);
                } //
                else if (reference.get(j).endsWith(",X")) {
                    n = 1;
                    i = 1;
                    x = 1;
                    getObjofF4(j);
                } else if (reference.get(j).matches("\\w+")) {
                    n = 1;
                    i = 1;
                    getObjofF4(j);
                }
            } else if (instructions.get(j).startsWith("&")) {
                getBinaryOp(j);
                x = 0;
                b = 0;
                p = 0;
                e = 0;
                if (reference.get(j).startsWith("#")) {
                    n = 0;
                    i = 1;
                    getHexOp2nixbpe(n, i, x, b, p, e);

                } else if (reference.get(j).startsWith("@")) {
                    n = 1;
                    i = 0;
                    getHexOp2nixbpe(n, i, x, b, p, e);

                } else {
                    n = 1;
                    i = 1;
                    getHexOp2nixbpe(n, i, x, b, p, e);
                }

                String splitInstruction = instructions.get(j).substring(1);//"LDA" without "&"
                int opCodeIndex = opInst.indexOf(splitInstruction);//index of LDA in converter file
                String operationCode = opCode.get(opCodeIndex);//opcode of LDA is 00

                String splitRef = reference.get(j).substring(0,reference.get(j).length()-2);
                int indexsplit = symbol.indexOf(splitRef);//index of label EXIT in symbol table
                String splitaddress = address.get(indexsplit);//get loc ctr found in symbol table of EXIT
                
                int regf = instructions.indexOf("REGF");//index of REGF
                String refofregf = reference.get(regf);//REGF CLOOP
                int indexref = symbol.indexOf(refofregf);//index of label CLOOP in symbol table
                String regfaddress = address.get(indexref); //get loc ctr found in symbol table of CLOOP

                String currentLoc = loc_ctr.get(j);//current location counter

                int F = (Integer.parseInt(regfaddress, 16) - Integer.parseInt(currentLoc, 16)) / 2;
                int dis = Integer.parseInt(splitaddress, 16) - F;
                disp = Integer.toHexString(dis);
                System.out.println("\nOP CODE " + operationCode);
                System.out.println("---------------------");

                String digitformat = String.format("%6s", disp).replace(' ', '0');
                obj = op1 + opnihex + xbpehex + digitformat;
                obj_code.add(obj);

            } //else if none of the above occurs then... 
            else {
                int opCodeIndex = opInst.indexOf(instructions.get(j));
                String format = opFormat.get(opCodeIndex);
                String operationCode = opCode.get(opCodeIndex);

                switch (format) {
                    case "1":
                        obj_code.add(operationCode);
                        break;

                    case "2":
                        List<String> split = new ArrayList<>();
                        if (reference.get(j).contains(",")) {
                            split.addAll(Arrays.asList(reference.get(j).split(",")));
                            for (int a = 0; a < split.size() - 1; a++) {
                                int index = register.indexOf(split.get(a));
                                String reg1 = registervalue.get(index);
                                int index2 = register.indexOf(split.get(a + 1));
                                String reg2 = registervalue.get(index2);
                                obj = operationCode + reg1 + reg2;
                            }
                            obj_code.add(obj);
                        } else {
                            int index = register.indexOf(reference.get(j));
                            String reg = registervalue.get(index);
                            obj = operationCode + reg + "0";
                            obj_code.add(obj);
                        }
                        break;

                    case "3":
                        e = 0;
                        getBinaryOp(j);
                        ///////
                        if (reference.get(j).startsWith("#")) {
                            n = 0;
                            i = 1;
                            x = 0;
                            if (reference.get(j).substring(1).matches("\\d+")) {
                                p = 0;
                                b = 0;
                                getHexOp2nixbpe(n, i, x, b, p, e);
                                int intref = Integer.parseInt(reference.get(j).substring(1));
                                disp = Integer.toHexString(intref);
                                String digitformat = String.format("%3s", disp).replace(' ', '0');
                                obj = op1 + opnihex + xbpehex + digitformat;

                                obj_code.add(obj);
                            } else if (reference.get(j).substring(1).matches("\\w+")) {
                                isBorP(j);
                            }
                        } else if (reference.get(j).startsWith("@")) {
                            n = 1;
                            i = 0;
                            x = 0;
                            isBorP(j);
                        } //
                        else if (reference.get(j).endsWith(",X")) {
                            n = 1;
                            i = 1;
                            x = 1;
                            isBorP(j);
                        } // 
                        else if (instructions.get(j).equalsIgnoreCase("RSUB")) {
                            n = 1;
                            i = 1;
                            x = 0;
                            p = 0;
                            b = 0;
                            getHexOp2nixbpe(n, i, x, b, p, e);
                            obj = op1 + opnihex + xbpehex + "000";

                            obj_code.add(obj);
                        } else {
                            n = 1;
                            i = 1;
                            x = 0;
                            isBorP(j);

                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public static void getHexOp2nixbpe(int n, int i, int x, int b, int p, int e) {

        //opni to concatinate part 2 of the opcode + n + i
        opni = dfOp2 + Integer.toString(n) + Integer.toString(i);
        System.out.println("opni " + opni);
        //opni to concatinate x + b + p + e
        xbpe = Integer.toString(x) + Integer.toString(b) + Integer.toString(p) + Integer.toString(e);
        System.out.println("xbpe " + xbpe);

        //convert string to binary
        bin1 = Integer.parseInt(opni, 2);
        bin2 = Integer.parseInt(xbpe, 2);

        //then convert to hex
        opnihex = Integer.toHexString(bin1);
        System.out.println("opnihex " + opnihex);
        xbpehex = Integer.toHexString(bin2);
        System.out.println("xbpehex " + xbpehex + "\n");
    }

    //
    public static void getBinaryOp(int j) {
        //get opcode of current instruction
        String operationCode = "";
        if (instructions.get(j).startsWith("+") || instructions.get(j).startsWith("&")) {
            int opCodeIndex = opInst.indexOf(instructions.get(j).substring(1));//substring gets what's after the "#"
            operationCode = opCode.get(opCodeIndex);
        } else {
            int opCodeIndex = opInst.indexOf(instructions.get(j));
            operationCode = opCode.get(opCodeIndex);
        }
        //gets the first digit of op code
        op1 = operationCode.substring(0, 1);

        //gets the second digit of op code
        op2 = operationCode.substring(1);

        //convert hex op code to binary
        binary1 = Integer.toBinaryString(Integer.parseInt(op1, 16));
        String dfOp1 = String.format("%4s", binary1).replace(' ', '0');  // "0   " --> "0000"

        binary2 = Integer.toBinaryString(Integer.parseInt(op2, 16));
        //if op2 is equal to 0 don't remove last 2 bits, keep it in 2-digit format
        if (Integer.parseInt(op2, 16) == 0) {
            dfOp2 = String.format("%2s", binary2).replace(' ', '0');

        }//else if it's any other number then remove the last 2 binary bits 
        else {
            binary2 = Integer.toBinaryString(Integer.parseInt(op2, 16)).substring(0, binary2.length() - 2);
            dfOp2 = String.format("%2s", binary2).replace(' ', '0');

        }
    }

    public static void isBorP(int j) {
        int disppc = 0;
        int fbase = instructions.indexOf("BASE");//index of base 3
        String refofbase = reference.get(fbase);//ref LENGTH
        int indexref = symbol.indexOf(refofbase);//index of LENGTH
        String base = address.get(indexref);//0033

        int index = 0;
        if (reference.get(j).startsWith("#") || reference.get(j).startsWith("@")) {
            index = symbol.indexOf(reference.get(j).substring(1));
        } else if (reference.get(j).endsWith(",X")) {
            index = symbol.indexOf(reference.get(j).substring(0, reference.get(j).length() - 2));
        } else {
            index = symbol.indexOf(reference.get(j));
        }
        disppc = Integer.parseInt(address.get(index), 16) - Integer.parseInt(loc_ctr.get(j + 1), 16); // 33-6

        if (-2048 <= disppc && disppc <= 2047) {
            p = 1;
            b = 0;
            disp = Integer.toHexString(disppc);
            String digitformat = String.format("%3s", disp).replace(' ', '0');
            if (disppc < 0) {
                disp = disp.substring(disp.length() - 3);
                getHexOp2nixbpe(n, i, x, b, p, e);
                obj = op1 + opnihex + xbpehex + disp;

                obj_code.add(obj);
            } else {
                getHexOp2nixbpe(n, i, x, b, p, e);
                obj = op1 + opnihex + xbpehex + digitformat;

                obj_code.add(obj);
            }

        } else {
            p = 0;
            b = 1;
            int dispbas = Integer.parseInt(address.get(index), 16) - Integer.parseInt(base, 16);
            disp = Integer.toHexString(dispbas);
            String digitformat = String.format("%3s", disp).replace(' ', '0');
            getHexOp2nixbpe(n, i, x, b, p, e);
            obj = op1 + opnihex + xbpehex + digitformat;

            obj_code.add(obj);
        }
    }

    public static void getObjofF4(int j) {
        int index = 0;
        if (reference.get(j).startsWith("#") || reference.get(j).startsWith("@")) {
            index = symbol.indexOf(reference.get(j).substring(1));
        } else if (reference.get(j).endsWith(",X")) {
            index = symbol.indexOf(reference.get(j).substring(0, reference.get(j).length() - 2));
        } else {
            index = symbol.indexOf(reference.get(j));
        }

        int loc = Integer.parseInt(address.get(index), 16);
        addr = Integer.toHexString(loc);
        String digitformat = String.format("%5s", addr).replace(' ', '0');
        getHexOp2nixbpe(n, i, x, b, p, e);
        obj = op1 + opnihex + xbpehex + digitformat;

        obj_code.add(obj);
    }

    //<-----HTE---------------------------------------------------------------------------------------------------------------->
    public static void HTE() {
        String s = loc_ctr.get(0);
        String start = String.format("%6s", s).replace(' ', '0');
        String e = loc_ctr.get(loc_ctr.size() - 1);
        String end = String.format("%6s", e).replace(' ', '0');
        int L = Integer.parseInt(end, 16) - Integer.parseInt(start, 16);
        String wl = Integer.toHexString(L);
        String wholeLength = String.format("%6s", wl).replace(' ', '0');
        //H record 
        String H = labels.get(0) + " ^ " + start + " ^ " + wholeLength;
        //E
        String E = start;
        //print all
        System.out.println("\nHTE:");
        System.out.println("H--> " + H);
        TRecord();
        System.out.println("E--> " + E);
    }
    //T Record
    //static variables to use inside the function
    static int count1 = 0;
    static int Lengthp = 0;
    static String endOfT = "";
    static String Start = "";
    static int len = 0;
//

    public static void TRecord() {
        String opcode = "";
        //loop over the instruction
        for (int u = 1; u < instructions.size(); u++) {

            if (!"------".equals(obj_code.get(u))) {
                opcode += " " + obj_code.get(u);   // to store object code 
                if (count1 == 0) { // loop to get the start of every record 
                    Start = loc_ctr.get(u);
                }
                endOfT = loc_ctr.get(u + 1); //store the end of every record
                len = Integer.parseInt(endOfT, 16) - Integer.parseInt(Start, 16);
                count1++;   // counter variable stores no of object code 
            }
            //
            if (u % 10 == 0 && u != 0) { // if u mod 10 is 0 then add linebreak
                String Record = "T" + " ^ " + Start + " ^ " + Integer.toHexString(len) + " ^ " + opcode;
                // to reset the values after every line break
                opcode = "";
                count1 = 0;
                Start = "";
                endOfT = "";
                len = 0;
                System.out.println(Record);
            }

        }

    }
}
