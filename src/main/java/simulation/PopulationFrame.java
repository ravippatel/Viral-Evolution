package simulation;

import config.Constant;
import geneticAlgorithm.GeneticAlgorithm;
import geneticAlgorithm.Virus;
import model.Person;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

/* This file shows evolution of individuals displays naive, recovered, vaccinated,
died, generations and delta variant.
 */

public class PopulationFrame extends JPanel implements ActionListener {

    private static int total_hours = 0;
    private final int height;
    private final int width;

    private final Random gen = new Random();
    private final GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm();
    private final int population = Constant.hostPopulation;
    private final Person[] p = new Person[population];
    private final Virus firstVariant;
    private final PopulationGraph populationGraph;
    private Virus secondVariant;
    private int firstVariantFitness = 0;
    private int secondVariantFitness = 1000;
    private int thirdVariantFitness = 1000;

    public PopulationFrame(int width, int height, PopulationGraph populationGraph) {
        this.width = width;
        this.height = height;
        this.populationGraph = populationGraph;
        setPreferredSize(new Dimension(width, height));
        for (int i = 0; i < population; i++) {
            int x = gen.nextInt(width);
            int y = gen.nextInt((height - Constant.FRAME_MIN_HEIGHT) + 1) + Constant.FRAME_MIN_HEIGHT;
            p[i] = new Person(x, y);
            if (i < population / 2) {
                p[i].actual_fitness = getRandomFitness(500, 600);
            } else {
                p[i].actual_fitness = getRandomFitness(600, 700);
            }
            p[i].fitness = p[i].actual_fitness;
            p[i].gene = getRandomGenoType();
        }

        firstVariant = getNewVariant(null, 1);
        firstVariantFitness = firstVariant.getFitness();

        System.out.println("first fitness" + firstVariantFitness);
        for (int i = 0; i < 10; i++) {
            int rand = gen.nextInt(1000);
            p[rand].infected = true;
            p[rand].main_virus = true;
            p[rand].no_infected_days = 1;
        }
        Timer TM = new Timer(100, this);
        TM.start();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color NaiveColor = Color.decode("#bdc3c7");
        for (int i = 0; i < population; i++) {
            g.setColor(NaiveColor);
            g.setFont(new Font("TimesRoman", Font.PLAIN, Constant.TEXT_HEIGHT));
            g.drawString("Naive", 25, Constant.TEXT_POSITION);

            if (((p[i].infected_main && p[i].fitness <= firstVariantFitness) || p[i].main_virus) && !p[i].delta_variant) {
                g.setFont(new Font("TimesRoman", Font.PLAIN, Constant.TEXT_HEIGHT));
                g.setColor(Color.red);
                Color InfectedOneColor = Color.decode("#c0392b");
                g.setColor(InfectedOneColor);
                g.drawString("Generation 1", 110, Constant.TEXT_POSITION);

                p[i].main_virus = true;
            }
            if (((p[i].infected_gen1 && p[i].fitness < secondVariantFitness) || p[i].gen1_virus) && !p[i].main_virus && !p[i].delta_variant) {
                g.setFont(new Font("TimesRoman", Font.PLAIN, Constant.TEXT_HEIGHT));
                Color InfectedTwoColor = Color.decode("#9b59b6");
                g.setColor(InfectedTwoColor);
                g.drawString("Generation 2", 260, Constant.TEXT_POSITION);
                p[i].gen1_virus = true;
            }
            if (p[i].recovered) {
                g.setFont(new Font("TimesRoman", Font.PLAIN, Constant.TEXT_HEIGHT));
                Color RecoveredColor = Color.decode("#e67e22");
                g.setColor(RecoveredColor);
                p[i].gen1_virus = false;
                p[i].main_virus = false;
                p[i].delta_variant = false;
                g.drawString("Recovered", 410, Constant.TEXT_POSITION);
            }
            if (p[i].vaccinated) {
                g.setFont(new Font("TimesRoman", Font.PLAIN, Constant.TEXT_HEIGHT));
                Color VaccinatedColor = Color.decode("#2ecc71");
                g.setColor(VaccinatedColor);
                g.drawString("Vaccinated", 560, Constant.TEXT_POSITION);
            }
            if (p[i].died) {
                g.setFont(new Font("TimesRoman", Font.PLAIN, Constant.TEXT_HEIGHT));
                Color Belize = Color.decode("#3498db");
                g.setColor(Belize);
                p[i].can_move = false;
                g.drawString("Died", 710, Constant.TEXT_POSITION);
            }
            if (((p[i].infected_delta && p[i].fitness < thirdVariantFitness) || p[i].delta_variant) && !p[i].main_virus && !p[i].gen1_virus) {
                g.setFont(new Font("TimesRoman", Font.PLAIN, Constant.TEXT_HEIGHT));
                Color Midnight = Color.decode("#2c3e50");
                g.setColor(Midnight);
                p[i].delta_variant = true;
                p[i].recovered = false;
                p[i].vaccinated = false;
                g.drawString("Delta Variant", 800, Constant.TEXT_POSITION);
            }
            g.fillOval(p[i].x, p[i].y, Constant.DOTS_SIZE, Constant.DOTS_SIZE);
        }
        if (total_hours == 400) {
            for (int k = 0; k < 10; k++) {
                int rand = gen.nextInt(1000);
                if (p[rand].died || p[rand].main_virus) {
                    k--;
                    continue;
                }
                p[rand].infected = true;
                p[rand].gen1_virus = true;
                p[rand].no_infected_days = 1;
            }
        }
        if (total_hours == 1500) {
            for (int k = 0; k < 5; k++) {
                int rand = gen.nextInt(1000);
                if (p[rand].died || p[rand].main_virus || p[rand].gen1_virus) {
                    k--;
                    continue;
                }
                p[rand].infected = true;
                p[rand].delta_variant = true;
                p[rand].no_infected_days = 1;
            }
        }
    }

    public void checkDistanceAndInfect() {
        for (int i = 0; i < population; i++) {
            for (int j = i + 1; j < population; j++) {
                int deltax = p[i].x - p[j].x;
                int deltay = p[i].y - p[j].y;
                double dist = Math.sqrt(deltax * deltax + deltay * deltay);
                if (dist <= Constant.INFECT_DISTANCE) {
                    if (p[j].main_virus) {
                        p[i].infected_main = true;
                    }
                    if (p[j].gen1_virus)
                        p[i].infected_gen1 = true;
                    if (p[j].delta_variant) {
                        int rand = gen.nextInt(2);
                        if (rand == 1) {
                            p[i].infected_delta = true;
                        }
                    }
                    if (p[i].infected_gen1 || p[i].infected_main || p[i].infected_delta)
                        p[i].no_infected_days++;
                }
            }
        }
    }

    public int infected() {
        int infected = 0;
        for (int i = 0; i < population; i++) {
            if (p[i].infected_main || p[i].infected_gen1 || p[i].infected_delta) {
                infected++;
            }
            if (p[i].recovered) {
                infected--;
            }
        }
        return infected;
    }

    public int totalRecovered() {
        int recovered = 0;
        for (int i = 0; i < population; i++) {
            if (p[i].recovered) {
                recovered++;
            }
        }
        return recovered;
    }

    public int totalVaccinated() {
        int vaccinated = 0;
        for (int i = 0; i < population; i++) {
            if (p[i].vaccinated) {
                vaccinated++;
            }
        }
        return vaccinated;
    }

    public int totalDied() {
        int died = 0;
        for (int i = 0; i < population; i++) {
            if (p[i].died) {
                died++;
            }
        }
        return died;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (total_hours <= 2920) {
            total_hours++;

            for (int i = 0; i < population; i++) {
                p[i].move();
                p[i].checkForImmunity();
            }

            if (total_hours == 300) {
                secondVariant = getNewVariant(firstVariant, 2);
                secondVariantFitness = secondVariant.getFitness();
            }
            if (total_hours == 1300) {
                Virus thirdVariant = getNewVariant(secondVariant, 3);
                thirdVariantFitness = thirdVariant.getFitness();
            }
            checkDistanceAndInfect();
            PopulationGraph.showChartVirusEvolution(infected(), population, totalRecovered(), totalVaccinated(), totalDied(), total_hours);

            repaint();
        }
    }

    public int getRandomFitness(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public String getRandomGenoType() {
        String[] geneType = {"A1", "A2", "B1", "B2"};
        int randIdx = new Random().nextInt(4);
        return geneType[randIdx];
    }

    public Virus getNewVariant(Virus previousVariant, int variantNumber) {
        return geneticAlgorithm.runGA(previousVariant, populationGraph, variantNumber);
    }
}
