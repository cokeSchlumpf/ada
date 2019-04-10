package ada.vcs.client.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

public class TestFactory {

    private TestFactory() {

    }

    public static Path createCSV(Path path) {
        return createCSV(path, "sample.csv");
    }

    public static Path createCSV(Path path, String filename) {
        final Path file = path.resolve(filename);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file.toFile(), false))) {
            writer.println("ID;Name;Seller;SomeNumber;Price;NoIdea;Something;Place;Category;Weight");

            String lines =
                "1;Eldon Base for stackable storage shelf, platinum;Muhammed MacIntyre;3;-213.25;38.94;35;Nunavut;Storage & Organization;0.8\n" +
                "2;\"1.7 Cubic Foot Compact \"\"Cube\"\" Office Refrigerators\";Barry French;293;457.81;208.16;68.02;Nunavut;Appliances;0.58\n" +
                "3;Cardinal Slant-DÆ Ring Binder, Heavy Gauge Vinyl;Barry French;293;46.71;Aug 69;Feb 99;Nunavut;Binders and Binder Accessories;0.39\n" +
                "4;R380;Clay Rozendal;483;1198.97;195.99;Mar 99;Nunavut;Telephones and Communication;0.58\n" +
                "5;Holmes HEPA Air Purifier;Carlos Soltero;515;30.94;21.78;May 94;Nunavut;Appliances;0.5\n" +
                "6;G.E. Longer-Life Indoor Recessed Floodlight Bulbs;Carlos Soltero;515;Apr 43;Jun 64;Apr 95;Nunavut;Office Furnishings;0.37\n" +
                "7;Angle-D Binders with Locking Rings, Label Holders;Carl Jackson;613;-54.04;07. Mar;Jul 72;Nunavut;Binders and Binder Accessories;0.38\n" +
                "8;SAFCO Mobile Desk Side File, Wire Frame;Carl Jackson;613;127.70;42.76;Jun 22;Nunavut;Storage & Organization;\n" +
                "9;SAFCO Commercial Wire Shelving, Black;Monica Federle;643;-695.26;138.14;35;Nunavut;Storage & Organization;\n" +
                "10;Xerox 198;Dorothy Badders;678;-226.36;Apr 98;Aug 33;Nunavut;Paper;0.38\n" +
                "11;Xerox 1980;Neola Schneider;807;-166.85;Apr 28;Jun 18;Nunavut;Paper;0.4\n" +
                "12;Advantus Map Pennant Flags and Round Head Tacks;Neola Schneider;807;-14.33;Mar 95;2;Nunavut;Rubber Bands;0.53\n" +
                "13;Holmes HEPA Air Purifier;Carlos Daly;868;134.72;21.78;May 94;Nunavut;Appliances;0.5\n" +
                "14;DS/HD IBM Formatted Diskettes, 200/Pack - Staples;Carlos Daly;868;114.46;47.98;Mar 61;Nunavut;Computer Peripherals;0.71\n" +
                "15;\"Wilson Jones 1\"\" Hanging DublLockÆ Ring Binders\";Claudia Miner;933;-4.72;May 28;Feb 99;Nunavut;Binders and Binder Accessories;0.37\n" +
                "16;Ultra Commercial Grade Dual Valve Door Closer;Neola Schneider;995;782.91;39.89;03. Apr;Nunavut;Office Furnishings;0.53\n" +
                "17;\"#10-4 1/8\"\" x 9 1/2\"\" Premium Diagonal Seam Envelopes\";Allen Rosenblatt;998;93.80;15.74;Jan 39;Nunavut;Envelopes;0.4\n" +
                "18;Hon 4-Shelf Metal Bookcases;Sylvia Foulston;1154;440.72;100.98;26.22;Nunavut;Bookcases;0.6\n" +
                "19;Lesro Sheffield Collection Coffee Table, End Table, Center Table, Corner Table;Sylvia Foulston;1154;-481.04;71.37;69;Nunavut;Tables;0.68\n" +
                "20;g520;Jim Radford;1344;-11.68;65.99;May 26;Nunavut;Telephones and Communication;0.59\n" +
                "21;LX 788;Jim Radford;1344;313.58;155.99;Aug 99;Nunavut;Telephones and Communication;0.58\n" +
                "22;Avery 52;Carlos Soltero;1412;26.92;Mar 69;0.5;Nunavut;Labels;0.38\n" +
                "23;Plymouth Boxed Rubber Bands by Plymouth;Carlos Soltero;1412;-5.77;Apr 71;0.7;Nunavut;Rubber Bands;0.8\n" +
                "24;\"GBC Pre-Punched Binding Paper, Plastic, White, 8-1/2\"\" x 11\"\"\";Carl Ludwig;1539;-172.88;15.99;13.18;Nunavut;Binders and Binder Accessories;0.37\n" +
                "25;\"Maxell 3.5\"\" DS/HD IBM-Formatted Diskettes, 10/Pack\";Carl Ludwig;1539;-144.55;Apr 89;Apr 93;Nunavut;Computer Peripherals;0.66\n" +
                "26;Newell 335;Don Miller;1540;May 76;Feb 88;0.7;Nunavut;Pens & Art Supplies;0.56\n" +
                "27;SANFORD Liquid Accentô Tank-Style Highlighters;Annie Cyprus;1702;Apr 90;Feb 84;0.93;Nunavut;Pens & Art Supplies;0.54\n" +
                "28;Canon PC940 Copier;Carl Ludwig;1761;-547.61;449.99;49;Nunavut;Copiers and Fax;0.38\n" +
                "29;Tenex Personal Project File with Scoop Front Design, Black;Carlos Soltero;1792;-5.45;13.48;Apr 51;Nunavut;Storage & Organization;0.59\n" +
                "30;Col-EraseÆ Pencils with Erasers;Grant Carroll;2275;41.67;06. Aug;Jan 17;Nunavut;Pens & Art Supplies;0.56\n" +
                "31;\"Imation 3.5\"\" DS/HD IBM Formatted Diskettes, 10/Pack\";Don Miller;2277;-46.03;May 98;Apr 38;Nunavut;Computer Peripherals;0.75\n" +
                "32;White Dual Perf Computer Printout Paper, 2700 Sheets, 1 Part, Heavyweight, 20 lbs., 14 7/8 x 11;Don Miller;2277;33.67;40.99;19.99;Nunavut;Paper;0.36\n" +
                "33;Self-Adhesive Address Labels for Typewriters by Universal;Alan Barnes;2532;140.01;Jul 31;0.49;Nunavut;Labels;0.38\n" +
                "34;Accessory37;Alan Barnes;2532;-78.96;20.99;02. May;Nunavut;Telephones and Communication;0.81\n" +
                "35;Fuji 5.2GB DVD-RAM;Jack Garza;2631;252.66;40.96;Jan 99;Nunavut;Computer Peripherals;0.55\n" +
                "36;Bevis Steel Folding Chairs;Julia West;2757;-1766.01;95.95;74.35;Nunavut;Chairs & Chairmats;0.57\n" +
                "37;Avery Binder Labels;Eugene Barchas;2791;-236.27;Mar 89;07. Jan;Nunavut;Binders and Binder Accessories;0.37\n" +
                "38;Hon Every-DayÆ Chair Series Swivel Task Chairs;Eugene Barchas;2791;80.44;120.98;30;Nunavut;Chairs & Chairmats;0.64\n" +
                "39;\"IBM Multi-Purpose Copy Paper, 8 1/2 x 11\"\", Case\";Eugene Barchas;2791;118.94;30.98;May 76;Nunavut;Paper;0.4\n" +
                "40;Global Troyô Executive Leather Low-Back Tilter;Edward Hooks;2976;3424.22;500.98;26;Nunavut;Chairs & Chairmats;0.6\n" +
                "41;XtraLifeÆ ClearVueô Slant-DÆ Ring Binders by Cardinal;Brad Eason;3232;-11.83;Jul 84;Apr 71;Nunavut;Binders and Binder Accessories;0.35\n" +
                "42;Computer Printout Paper with Letter-Trim Perforations;Nicole Hansen;3524;52.35;18.97;09. Mar;Nunavut;Paper;0.37\n" +
                "43;6160;Dorothy Wardle;3908;-180.20;115.99;02. May;Nunavut;Telephones and Communication;0.57\n" +
                "44;Avery 49;Aaron Bergman;4132;Jan 32;Feb 88;0.5;Nunavut;Labels;0.36\n" +
                "45;Hoover Portapowerô Portable Vacuum;Jim Radford;4612;-375.64;Apr 48;49;Nunavut;Appliances;0.6\n" +
                "46;Timeport L7089;Annie Cyprus;4676;-104.25;125.99;Jul 69;Nunavut;Telephones and Communication;0.58\n" +
                "47;Avery 510;Annie Cyprus;4676;85.96;Mar 75;0.5;Nunavut;Labels;0.37\n" +
                "48;Xerox 1881;Annie Cyprus;4676;-8.38;Dec 28;Jun 47;Nunavut;Paper;0.38\n" +
                "49;LX 788;Annie Cyprus;4676;1115.69;155.99;Aug 99;Nunavut;Telephones and Communication;0.58";

            writer.println(lines);

            return file;
        } catch (IOException e) {
            return ExceptionUtils.wrapAndThrow(e);
        }
    }

}
