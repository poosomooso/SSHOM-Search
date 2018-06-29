package tcas;

public class Tcas {
    public static int OLEV = 600;
    public static int MAXALTDIFF = 600;
    public static int MINSEP = 300;
    public static int NOZCROSS = 100;


    public static int Cur_Vertical_Sep;
    public static boolean High_Confidence;
    public static boolean Two_of_Three_Reports_Valid;

    public static int Own_Tracked_Alt;
    public static int Own_Tracked_Alt_Rate;
    public static int Other_Tracked_Alt;

    public static int Alt_Layer_Value;		/* 0, 1, 2, 3 */
    //public static int[] Positive_RA_Alt_Thresh;
    static int Positive_RA_Alt_Thresh_0;
    static int Positive_RA_Alt_Thresh_1;
    static int Positive_RA_Alt_Thresh_2;
    static int Positive_RA_Alt_Thresh_3;

    public static int Up_Separation;
    public static int Down_Separation;


    /* state variables */
    public static int Other_RAC;			/* NO_INTENT, DO_NOT_CLIMB, DO_NOT_DESCEND */
    public static int NO_INTENT = 0;
    public static int DO_NOT_CLIMB = 1;
    public static int DO_NOT_DESCEND = 2;

    public static int Other_Capability;		/* TCAS_TA, OTHER */
    public static int TCAS_TA = 1;
    public static int OTHER = 2;

    public static int Climb_Inhibit;		/* true/false */

    public static int UNRESOLVED = 0;
    public static int UPWARD_RA = 1;
    public static int DOWNWARD_RA = 2;

    void initialize() {
        Positive_RA_Alt_Thresh_0 = 400;
        Positive_RA_Alt_Thresh_1 = 500;
        Positive_RA_Alt_Thresh_2 = 640;
        Positive_RA_Alt_Thresh_3 = 740;
    }

    int ALIM() {
        if (Alt_Layer_Value == 0)
            return Positive_RA_Alt_Thresh_0;
        else if (Alt_Layer_Value == 1)
            return Positive_RA_Alt_Thresh_1;
        else if (Alt_Layer_Value == 2)
            return Positive_RA_Alt_Thresh_2;
        else
            return Positive_RA_Alt_Thresh_3;
    }

    int Inhibit_Biased_Climb() {
        if (Climb_Inhibit > 0)
            return Up_Separation + NOZCROSS;
        else
            return Up_Separation;
        //return (Climb_Inhibit ? Up_Separation + NOZCROSS : Up_Separation);
    }

    boolean Non_Crossing_Biased_Climb() {
        int upward_preferred;
        int upward_crossing_situation;
        boolean result;
        if (Inhibit_Biased_Climb() > Down_Separation) {
            upward_preferred = 1;
        } else {
            upward_preferred = 0;
        }
        if (upward_preferred != 0) {

            result = !(Own_Below_Threat()) || ((Own_Below_Threat()) && (!(Down_Separation > ALIM())));
        } else {
            result = Own_Above_Threat() && (Cur_Vertical_Sep >= MINSEP) && (Up_Separation >= ALIM());
        }
        return result;
    }

    boolean Non_Crossing_Biased_Descend() {
        int upward_preferred;
        int upward_crossing_situation;
        boolean result;
        if (Inhibit_Biased_Climb() > Down_Separation) {
            upward_preferred = 1;
        } else {
            upward_preferred = 0;
        }
        //upward_preferred = Inhibit_Biased_Climb() > Down_Separation;
        if (upward_preferred != 0) {
            result = Own_Below_Threat() && (Cur_Vertical_Sep >= MINSEP) && (Down_Separation >= ALIM());
        } else {
            result = !(Own_Above_Threat()) || ((Own_Above_Threat()) && (Up_Separation >= ALIM()));
        }
        return result;
    }

    boolean Own_Below_Threat() {
        return (Own_Tracked_Alt < Other_Tracked_Alt);
    }

    boolean Own_Above_Threat() {
        return (Other_Tracked_Alt < Own_Tracked_Alt);
    }

    int alt_sep_test() {
        boolean enabled, tcas_equipped, intent_not_known;
        boolean need_upward_RA, need_downward_RA;
        int alt_sep;

        enabled = High_Confidence && (Own_Tracked_Alt_Rate <= OLEV) && (Cur_Vertical_Sep > MAXALTDIFF);
        tcas_equipped = Other_Capability == TCAS_TA;
        intent_not_known = Two_of_Three_Reports_Valid && Other_RAC == NO_INTENT;

        alt_sep = UNRESOLVED;

        if (enabled && ((tcas_equipped && intent_not_known) || !tcas_equipped)) {
            need_upward_RA = Non_Crossing_Biased_Climb() && Own_Below_Threat();
            need_downward_RA = Non_Crossing_Biased_Descend() && Own_Above_Threat();
            if (need_upward_RA && need_downward_RA)
                /* unreachable: requires Own_Below_Threat and Own_Above_Threat
                   to both be true - that requires Own_Tracked_Alt < Other_Tracked_Alt
                   and Other_Tracked_Alt < Own_Tracked_Alt, which isn't possible */
                alt_sep = UNRESOLVED;
            else if (need_upward_RA)
                alt_sep = UPWARD_RA;
            else if (need_downward_RA)
                alt_sep = DOWNWARD_RA;
            else
                alt_sep = UNRESOLVED;
        }

        return alt_sep;
    }

    public int begin(int num, int cur_vertical_sep, int high_conf, int two_thirds_reports_valid, int own_tracked_alt,
                     int own_tracked_alt_rate, int other_tracked_alt, int alt_layer_val, int up_sep, int down_sep,
                     int other_rac, int other_capability, int climb_inhibit) {

        if (num < 13) {
            System.out.println("Error: Command line arguments are");
            System.out.println("Cur_Vertical_Sep, High_Confidence, Two_of_Three_Reports_Valid");
            System.out.println("Own_Tracked_Alt, Own_Tracked_Alt_Rate, Other_Tracked_Alt");
            System.out.println("Alt_Layer_Value, Up_Separation, Down_Separation");
            System.out.println("Other_RAC, Other_Capability, Climb_Inhibit");
            return -1;
        }
        initialize();
        Cur_Vertical_Sep = cur_vertical_sep;
        if (high_conf == 0) {
            High_Confidence = false;
        } else {
            High_Confidence = true;
        }
        if (two_thirds_reports_valid == 0) {
            Two_of_Three_Reports_Valid = false;
        } else {
            Two_of_Three_Reports_Valid = true;
        }
        Own_Tracked_Alt = own_tracked_alt;
        Own_Tracked_Alt_Rate = own_tracked_alt_rate;
        Other_Tracked_Alt = other_tracked_alt;
        Alt_Layer_Value = alt_layer_val;
        Up_Separation = up_sep;
        Down_Separation = down_sep;
        Other_RAC = other_rac;
        Other_Capability = other_capability;
        Climb_Inhibit = climb_inhibit;

        int res = alt_sep_test();
        System.out.println(res);
        return res;
    }

}



