import { useState } from "react";
import { Calendar } from "../components/calendar";
import { Card, CardContent, CardHeader, CardTitle } from "../components/card";
import { Button } from "../components/button";
import { Input } from "../components/input";
import { Label } from "../components/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/select";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "../components/dialog";
import { Badge } from "../components/badge";
import { ArrowLeft, Plus, Calendar as CalendarIcon, Users, CheckCircle2 } from "lucide-react";
import { format } from "date-fns";
import { motion } from "motion/react";

interface Project {
  id: string;
  title: string;
  client: string;
  team: string;
  dueDate: Date;
  status: "pending" | "in-progress" | "completed";
}

interface CalendarAdminProps {
  onNavigate: (page: string) => void;
}

const mockTeams = [
  "Team Alpha - Construction",
  "Team Beta - Engineering",
  "Team Gamma - Technology",
  "Team Delta - Design",
];

export default function CalendarAdmin({ onNavigate }: CalendarAdminProps) {
  const [date, setDate] = useState<Date | undefined>(new Date());
  const [projects, setProjects] = useState<Project[]>([
    {
      id: "1",
      title: "Office Building Renovation",
      client: "ABC Corp",
      team: "Team Alpha - Construction",
      dueDate: new Date(2025, 10, 15),
      status: "in-progress",
    },
    {
      id: "2",
      title: "Smart Home Integration",
      client: "Tech Homes Inc",
      team: "Team Gamma - Technology",
      dueDate: new Date(2025, 10, 20),
      status: "pending",
    },
    {
      id: "3",
      title: "Bridge Structural Analysis",
      client: "City Infrastructure",
      team: "Team Beta - Engineering",
      dueDate: new Date(2025, 10, 8),
      status: "completed",
    },
  ]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [newProject, setNewProject] = useState({
    title: "",
    client: "",
    team: "",
    dueDate: new Date(),
    status: "pending" as const,
  });

  const handleAddProject = () => {
    if (newProject.title && newProject.client && newProject.team) {
      setProjects([
        ...projects,
        {
          id: Date.now().toString(),
          ...newProject,
        },
      ]);
      setNewProject({
        title: "",
        client: "",
        team: "",
        dueDate: new Date(),
        status: "pending",
      });
      setIsDialogOpen(false);
    }
  };

  const getProjectsForDate = (date: Date) => {
    return projects.filter(
      (project) => format(project.dueDate, "yyyy-MM-dd") === format(date, "yyyy-MM-dd")
    );
  };

  const selectedDateProjects = date ? getProjectsForDate(date) : [];

  const getStatusColor = (status: string) => {
    switch (status) {
      case "completed":
        return "bg-yellow-400 text-black border-yellow-500";
      case "in-progress":
        return "bg-yellow-400/50 text-black border-yellow-400/70";
      default:
        return "bg-yellow-400/20 text-white border-yellow-400/40";
    }
  };

  return (
    <div className="min-h-screen bg-black">
      {/* Header - Liquid Glass Style */}
      <div className="bg-black/60 backdrop-blur-xl border-b border-yellow-400/20 shadow-2xl sticky top-0 z-40">
        <div className="container mx-auto px-6 py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <Button
                onClick={() => onNavigate("home")}
                variant="ghost"
                className="text-white hover:text-yellow-400 hover:bg-white/10"
              >
                <ArrowLeft className="mr-2 h-5 w-5" />
                BACK TO HOME
              </Button>
              <h1 className="text-3xl text-white tracking-wide">ADMIN CALENDAR</h1>
            </div>
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button className="bg-yellow-400 text-black hover:bg-yellow-500">
                  <Plus className="mr-2 h-5 w-5" />
                  NEW PROJECT
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[500px] bg-black border border-yellow-400/30">
                <DialogHeader>
                  <DialogTitle className="text-white">Create New Project</DialogTitle>
                </DialogHeader>
                <div className="space-y-4 py-4">
                  <div className="space-y-2">
                    <Label htmlFor="title" className="text-white/70">Project Title</Label>
                    <Input
                      id="title"
                      value={newProject.title}
                      onChange={(e) => setNewProject({ ...newProject, title: e.target.value })}
                      placeholder="Enter project title"
                      className="bg-white/5 border-yellow-400/20 text-white placeholder:text-white/30"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="client" className="text-white/70">Client Name</Label>
                    <Input
                      id="client"
                      value={newProject.client}
                      onChange={(e) => setNewProject({ ...newProject, client: e.target.value })}
                      placeholder="Enter client name"
                      className="bg-white/5 border-yellow-400/20 text-white placeholder:text-white/30"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="team" className="text-white/70">Assign Team</Label>
                    <Select
                      value={newProject.team}
                      onValueChange={(value) => setNewProject({ ...newProject, team: value })}
                    >
                      <SelectTrigger className="bg-white/5 border-yellow-400/20 text-white">
                        <SelectValue placeholder="Select a team" />
                      </SelectTrigger>
                      <SelectContent className="bg-black border-yellow-400/30">
                        {mockTeams.map((team) => (
                          <SelectItem key={team} value={team} className="text-white focus:bg-yellow-400/20 focus:text-white">
                            {team}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label className="text-white/70">Due Date</Label>
                    <Calendar
                      mode="single"
                      selected={newProject.dueDate}
                      onSelect={(date) => date && setNewProject({ ...newProject, dueDate: date })}
                      className="rounded-md border border-yellow-400/20 bg-white/5 text-white"
                    />
                  </div>
                  <Button
                    onClick={handleAddProject}
                    className="w-full bg-yellow-400 text-black hover:bg-yellow-500"
                  >
                    CREATE PROJECT
                  </Button>
                </div>
              </DialogContent>
            </Dialog>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container mx-auto px-6 py-8">
        <div className="grid lg:grid-cols-3 gap-8">
          {/* Calendar Section - Glassmorphism */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="lg:col-span-2"
          >
            <div className="relative group">
              <div className="absolute -inset-1 bg-gradient-to-r from-yellow-400/20 to-transparent rounded-3xl blur opacity-50"></div>
              <Card className="relative bg-white/5 backdrop-blur-xl border-yellow-400/20 shadow-2xl">
                <CardHeader>
                  <CardTitle className="flex items-center gap-2 text-2xl text-white tracking-wide">
                    <CalendarIcon className="h-6 w-6 text-yellow-400" />
                    PROJECT CALENDAR
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <Calendar
                    mode="single"
                    selected={date}
                    onSelect={setDate}
                    className="rounded-md border border-yellow-400/20 w-full bg-black/30 text-white"
                    modifiers={{
                      hasProject: projects.map((p) => p.dueDate),
                    }}
                    modifiersStyles={{
                      hasProject: {
                        backgroundColor: "#facc15",
                        color: "black",
                        fontWeight: "bold",
                      },
                    }}
                  />
                  <div className="mt-6 p-4 bg-yellow-400/10 rounded-lg border border-yellow-400/30">
                    <p className="text-sm text-white/70">
                      <strong className="text-yellow-400">Tip:</strong> Dates highlighted in yellow have scheduled projects. Click a
                      date to view details.
                    </p>
                  </div>
                </CardContent>
              </Card>
            </div>
          </motion.div>

          {/* Projects for Selected Date - Glassmorphism */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 }}
          >
            <div className="relative group">
              <div className="absolute -inset-1 bg-gradient-to-r from-yellow-400/20 to-transparent rounded-3xl blur opacity-50"></div>
              <Card className="relative bg-white/5 backdrop-blur-xl border-yellow-400/20 shadow-2xl">
                <CardHeader>
                  <CardTitle className="text-xl text-white tracking-wide">
                    {date ? format(date, "MMMM d, yyyy") : "SELECT A DATE"}
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {selectedDateProjects.length > 0 ? (
                    <div className="space-y-4">
                      {selectedDateProjects.map((project) => (
                        <div
                          key={project.id}
                          className="p-4 border-l-4 border-yellow-400 bg-black/40 backdrop-blur-sm rounded-lg shadow"
                        >
                          <h4 className="mb-2 tracking-wide text-white">{project.title}</h4>
                          <p className="text-sm text-gray-300 mb-2">Client: {project.client}</p>
                          <div className="flex items-center gap-2 text-sm text-gray-300 mb-2">
                            <Users className="h-4 w-4" />
                            {project.team}
                          </div>
                          <Badge className={getStatusColor(project.status)}>{project.status}</Badge>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-8 text-gray-400">
                      <CalendarIcon className="h-12 w-12 mx-auto mb-4 text-gray-600" />
                      <p>No projects scheduled for this date</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          </motion.div>
        </div>

        {/* All Projects List - Glassmorphism */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.2 }}
          className="mt-8"
        >
          <div className="relative group">
            <div className="absolute -inset-1 bg-gradient-to-r from-yellow-400/20 to-transparent rounded-3xl blur opacity-50"></div>
            <Card className="relative bg-white/5 backdrop-blur-xl border-yellow-400/20 shadow-2xl">
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-2xl text-white tracking-wide">
                  <CheckCircle2 className="h-6 w-6 text-yellow-400" />
                  ALL PROJECTS
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {projects.map((project) => (
                    <div
                      key={project.id}
                      className="flex items-center justify-between p-4 bg-black/40 backdrop-blur-sm border border-yellow-400/20 rounded-lg hover:border-yellow-400/40 transition-all hover:shadow-lg hover:shadow-yellow-400/10"
                    >
                      <div>
                        <h4 className="mb-1 text-white">{project.title}</h4>
                        <p className="text-sm text-gray-300">Client: {project.client}</p>
                        <div className="flex items-center gap-2 text-sm text-gray-300 mt-1">
                          <Users className="h-4 w-4" />
                          {project.team}
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="text-sm text-gray-300 mb-2">
                          Due: {format(project.dueDate, "MMM d, yyyy")}
                        </p>
                        <Badge className={getStatusColor(project.status)}>{project.status}</Badge>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
